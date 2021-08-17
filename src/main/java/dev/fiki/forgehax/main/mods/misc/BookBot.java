package dev.fiki.forgehax.main.mods.misc;

import com.google.common.collect.Lists;
import dev.fiki.forgehax.api.cmd.argument.Arguments;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.LongSetting;
import dev.fiki.forgehax.api.cmd.settings.StringSetting;
import dev.fiki.forgehax.api.extension.ItemEx;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.EditBookScreen;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.play.client.CEditBookPacket;
import net.minecraft.util.Hand;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Scanner;
import java.util.function.Consumer;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "BookBot",
    description = "Automatically write books",
    category = Category.MISC
)
@ExtensionMethod({ItemEx.class, LocalPlayerEx.class})
public class BookBot extends ToggleMod {

  private static final int MAX_CHARACTERS_PER_PAGE = 256;
  private static final int MAX_PAGES = 100;

  public static final String NUMBER_TOKEN = "\\{NUMBER\\}";

  public static final String NEW_PAGE = ":PAGE:";

  private final StringSetting name = newStringSetting()
      .name("name")
      .description("Name of the book, use {NUMBER} for the number")
      .defaultTo("Book #{NUMBER}")
      .changedListener((from, to) -> {
        // 3 digits seems like a reasonable upper limit
        String str = to.replaceAll(NUMBER_TOKEN, "XXX");
        if (str.length() > 32) {
          printWarning("Final book names longer than 32 letters will cause crashes!"
              + "Current length (assuming 3 digits): %d", str.length());
        }
      })
      .build();

  private final BooleanSetting sign = newBooleanSetting()
      .name("sign")
      .description("finalize the book with title and authorship")
      .defaultTo(true)
      .build();

  private final StringSetting file = newStringSetting()
      .name("file")
      .description("Name of the file inside the forgehax directory to use")
      .defaultTo("")
      .build();

  private final BooleanSetting prettify = newBooleanSetting()
      .name("prettify")
      .description("Enables word wrapping. Can cause book size to increase dramatically")
      .defaultTo(true)
      .build();

  private final LongSetting sleep = newLongSetting()
      .name("sleep")
      .description("Sleep time in ms")
      .defaultTo(300L)
      .build();

  private Thread writerThread = null;
  private BookWriter writer = null;

  {
    newSimpleCommand()
        .name("start")
        .description("Start book bot. Can optionally set the starting position")
        .argument(Arguments.newIntegerArgument()
            .label("page")
            .optional()
            .defaultValue(0)
            .build())
        .executor(args -> {
          if (writerThread != null) {
            throw new RuntimeException("BookBot thread already running!");
          }

          Integer page = args.<Integer>getFirst().getValue();

          if (writer == null) {
            writer = loadFile();
            args.inform(String.format("BookBot file \"%s\" loaded successfully", file.getValue()));
          }

          writer.setPage(page);
          writerThread = new Thread(writer);
          writer.start();
          writerThread.start();
          args.inform("BookBot task started");
        })
        .build();

    newSimpleCommand()
        .name("reset")
        .description("Stop the BookBot task")
        .executor(args -> {
          if (writer != null) {
            writer.setFinalListener(o -> args.inform("BookBot task stopped at page " + writer.getPage()));
            writer.stop();
            writerThread = null;
            args.inform("Stopping BookBot");
          } else {
            args.warn("No writer present");
          }
        })
        .build();

    newSimpleCommand()
        .name("resume")
        .description("Resume the BookBot task")
        .executor(args -> {
          if (writer != null) {
            writerThread = new Thread(writer);
            writer.start();
            writerThread.start();
          } else {
            args.warn("No writer present");
          }
        })
        .build();

    newSimpleCommand()
        .name("delete")
        .description("Delete the writer bot instance")
        .executor(args -> {
          if (writer != null) {
            writer.setFinalListener(
                o -> args.inform("BookBot task stopped at page " + writer.getPage()));
            writer.stop();
            writer = null;
            writerThread = null;
            args.inform("Shutting down BookBot instance");
          } else {
            args.warn("No writer present");
          }
        })
        .build();

    newSimpleCommand()
        .name("load")
        .description("Load the file into memory")
        .executor(args -> {
          writer = loadFile();
          args.inform(String.format("BookBot file \"%s\" loaded successfully", file.getValue()));
        })
        .build();

    newSimpleCommand()
        .name("save")
        .description("Save the contents to a .book file in the forgehax folder")
        .executor(args -> {
          String fname = args.getFirst().getStringValue();

          // optional argument, if not given use name from file variable and rename the
          // extension to .book
          if (fname == null || fname.isEmpty()) {
            fname = file.getValue();
            if (!fname.endsWith(".book")) {
              fname = fname.substring(0, fname.lastIndexOf('.'));
            }
          }
          if (!fname.endsWith(".book")) {
            fname += ".book"; // append extension type
          }

          if (writer != null) {
            try (BufferedWriter out = Files.newBufferedWriter(
                getFileManager().getBaseResolve(fname),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
              out.write(writer.contents);
              args.inform("Successfully saved book data");
            } catch (IOException e) {
              args.warn("Failed to write file");
            }
          } else {
            args.warn("No writer present");
          }
        })
        .build();
  }

  private static final Collection<Character> CHARS_NO_REPEATING =
      Lists.newArrayList(' ', '\n', '\t', '\r');

  private static String parseText(String text, boolean wrap) {
    text = text.replace('\r', '\n').replace('\t', ' ').replace("\0", "");

    StringBuilder builder = new StringBuilder();

    char next = '\0', last;
    int ls = -1; // last space index
    for (int i = 0, p = i; i < text.length(); i++, p++, p %= MAX_CHARACTERS_PER_PAGE) {
      // previous character
      last = next;
      // next character
      next = text.charAt(i);

      // start a new page at the initial position
      if (p == 0) {
        builder.append(NEW_PAGE);
      }

      // if this index contains a space, save the index
      if (next == ' ') {
        ls = i;
      }

      // prevent annoying repeating characters
      if (CHARS_NO_REPEATING.contains(next) && CHARS_NO_REPEATING.contains(last)) {
        // do not append, go back 1 position to act as if this was never processed
        p--;
        continue;
      }

      // word wrapping logic
      if (wrap && ls != -1 && last == ' ') {
        // next space index
        int ns = text.indexOf(' ', i);
        // distance from next space to last space
        int d = ns - ls;

        // if the word (distance between two spaces) is less than the max chars allowed (to prevent
        // words greater than it from causing an infinite loop), and
        // the word will not fit onto the current page.
        if (d < MAX_CHARACTERS_PER_PAGE && (p + d) > MAX_CHARACTERS_PER_PAGE) {
          // insert new page
          builder.append(NEW_PAGE);
          // start at position 0
          p = 0;
        }
      }

      builder.append(next);
    }

    return builder.toString();
  }

  private BookWriter loadFile() throws RuntimeException {
    if (file.getValue().isEmpty()) {
      throw new RuntimeException("No file name set");
    }

    Path data = getFileManager().getBaseResolve(file.getValue());

    if (!Files.exists(data)) {
      throw new RuntimeException("File not found");
    }
    if (!Files.isRegularFile(data)) {
      throw new RuntimeException("Not a file type");
    }

    String text;
    try {
      text = new String(Files.readAllBytes(data), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read file");
    }

    String name = data.getFileName().toString();
    if (name.endsWith(".txt") || name.endsWith(".book")) {
      return new BookWriter(this, name.endsWith(".txt") ? parseText(text, prettify.getValue()) : text,
          sign.getValue());
    } else {
      throw new RuntimeException("File is not a .txt or .book type");
    }
  }

  @Override
  public String getDisplayText() {
    return this.writer == null
        ? super.getDisplayText()
        : super.getDisplayText() + "[" + this.writer.toString() + "]";
  }

  private static class BookWriter implements Runnable {

    public enum Status {
      INITIALIZED,
      FINISHED,
      ERROR,
      STOPPED,
      AWAITING_GUI_CLOSE,
      NEED_EMPTY_BOOKS_IN_HOTBAR,
      CHANGING_HELD_ITEM,
      OPENING_BOOK,
      CLOSING_BOOK,
      WRITING_BOOK,
    }

    private final BookBot parent;
    private final String contents;
    private final boolean signBook;

    private final int totalPages;

    private volatile Status status = Status.INITIALIZED;
    private volatile boolean stopped = false;

    private Scanner parser;

    private int page = 0;

    private Consumer<BookWriter> finalListener = null;

    public BookWriter(BookBot parent, String contents, boolean signBook) {
      this.parent = parent;
      this.contents = contents;
      this.signBook = signBook;
      Scanner scanner = newScanner();

      int c = 0;
      while (scanner.hasNext()) {
        scanner.next();
        c++;
      }
      this.totalPages = c;
    }

    private Scanner newScanner() {
      return new Scanner(contents).useDelimiter(NEW_PAGE);
    }

    public int getTotalPages() {
      return totalPages;
    }

    public int getTotalBooks() {
      return totalPages > 0 ? (int) Math.ceil((double) (totalPages) / (double) (MAX_PAGES)) : 0;
    }

    public Status getStatus() {
      return status;
    }

    public int getPage() {
      return page;
    }

    public void setPage(int page) {
      if (parser != null) {
        throw new RuntimeException("Cannot set position while task is running or stopped");
      }
      this.page = page;
    }

    public int getBook() {
      return page > 0 ? (int) Math.ceil((double) (page) / (double) (MAX_PAGES)) : 0;
    }

    public void setFinalListener(Consumer<BookWriter> finalListener) {
      this.finalListener = finalListener;
    }

    public boolean isStopped() {
      return stopped;
    }

    public void start() {
      if (parser == null) {
        parser = newScanner();

        // skip pages
        for (int i = 0; i < page && parser.hasNext(); i++) {
          parser.next();
        }
      }
      stopped = false;
      finalListener = null;
    }

    public void stop() {
      stopped = true;
    }

    private void sendBook(ItemStack stack) {
      ListNBT pages = new ListNBT(); // page tag list

      // copy pages into NBT
      for (int i = 0; i < MAX_PAGES && parser.hasNext(); i++) {
        pages.add(StringNBT.valueOf(parser.next().trim()));
        page++;
      }

      // set our client side book
      stack.addTagElement("pages", pages);

      // publish the book
      if (signBook) {
        stack.addTagElement("author",
            StringNBT.valueOf(getLocalPlayer().getGameProfile().getName()));
        stack.addTagElement("title", StringNBT.valueOf(parent.name.getValue()
            .replaceAll(NUMBER_TOKEN, "" + getBook())
            .trim()));
      }
      sendNetworkPacket(new CEditBookPacket(stack, signBook, getLocalPlayer().getSelectedIndex()));
    }

    @Override
    public void run() {
      try {
        while (!stopped) {
          // check to see if we've finished the book
          if (!parser.hasNext()) {
            this.status = Status.FINISHED;
            break;
          }

          sleep();

          // wait for screen
          if (getDisplayScreen() != null) {
            this.status = Status.AWAITING_GUI_CLOSE;
            continue;
          }

          // search for empty book
          final ClientPlayerEntity lp = getLocalPlayer();
          final Slot selected = lp.getHotbarSlots().stream()
              .filter(s -> {
                final ItemStack stack = s.getItem();
                return !stack.isEmpty()
                    && Items.WRITABLE_BOOK.equals(stack.getItem())
                    // written but unsigned books
                    && (stack.getTag() == null || stack.getTag().get("pages") == null);
              })
              .findAny()
              .orElse(null);

          // make sure we found a book
          if (selected == null) {
            this.status = Status.NEED_EMPTY_BOOKS_IN_HOTBAR;
            continue;
          }

          // set selected item to that slot
          while (lp.getSelectedSlot() != selected) {
            lp.forceSelectedSlot(selected);
            this.status = Status.CHANGING_HELD_ITEM;
            sleep();
          }

          final ItemStack item = selected.getItem();

          // open the book gui screen
          this.status = Status.OPENING_BOOK;
          addScheduledTask(() -> getLocalPlayer().openItemGui(item, Hand.MAIN_HAND));

          // wait for gui to open
          while (!(getDisplayScreen() instanceof EditBookScreen)) {
            sleep();
          }

          // send book to server
          this.status = Status.WRITING_BOOK;
          addScheduledTask(() -> {
            sendBook(item);
            setDisplayScreen(null);
          });

          // wait for screen to close
          while (getDisplayScreen() != null) {
            sleep();
          }
        }
      } catch (Throwable t) {
        this.status = Status.ERROR;
      } finally {
        if (finalListener != null) {
          finalListener.accept(this);
          finalListener = null;
        }

        // set stopped to true
        this.stopped = true;

        if (!this.status.equals(Status.FINISHED) && !this.status.equals(Status.ERROR)) {
          this.status = Status.STOPPED;
        }
      }
    }

    @Override
    public String toString() {
      return String.format(
          "Status=%s,P/T=%d/%d,B/T=%d/%d",
          status.name(), page, getTotalPages(), getBook(), getTotalBooks());
    }

    private void sleep() throws InterruptedException {
      Thread.sleep(parent.sleep.getValue());
      if (stopped) {
        throw new RuntimeException("Thread stopped");
      }
    }
  }
}
