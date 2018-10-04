package com.matt.forgehax.util.command.v2;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.command.v2.argument.*;
import com.matt.forgehax.util.command.v2.converter.ConverterFactory;
import com.matt.forgehax.util.command.v2.exception.CmdAmbiguousException;
import com.matt.forgehax.util.command.v2.exception.CmdMissingArgumentException;
import com.matt.forgehax.util.command.v2.exception.CmdRuntimeException;
import com.matt.forgehax.util.command.v2.exception.CmdUnknownException;
import com.matt.forgehax.util.command.v2.serializers.CmdChildrenSerializer;
import com.matt.forgehax.util.command.v2.serializers.CmdFlagSerializer;
import com.matt.forgehax.util.serialization.Serializers;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

/** Created on 12/26/2017 by fr1kin */
public class ParentCmd extends AbstractCmd implements IParentCmd {
  private static final Collection<IArg<?>> ARGUMENTS =
      Collections.singleton(
          new ArgBuilder<ICmd>()
              .description("child command")
              .shortDescription("cmd")
              .required()
              .converter(
                  ConverterFactory.<ICmd>newBuilder()
                      .type(ICmd.class)
                      .label("cmd")
                      .comparator(CmdHelper::compare)
                      .valuesOf(
                          (input, stream) ->
                              stream
                                  .filter(
                                      cmd ->
                                          cmd.getName()
                                              .toLowerCase()
                                              .startsWith(input.toLowerCase()))
                                  .sorted(
                                      Comparator.comparing(
                                          cmd -> cmd.getName().toLowerCase(),
                                          Comparator.<String>comparingInt(
                                                  name ->
                                                      StringUtils.getLevenshteinDistance(
                                                          name, input))
                                              .thenComparing(String::compareToIgnoreCase))))
                      .toString(ICmd::getName)
                      .serializer(Serializers.nullSerializer())
                      .build())
              .build());

  private final List<ICmd> children = Lists.newCopyOnWriteArrayList();

  public ParentCmd(
      String name,
      @Nullable Collection<String> aliases,
      String description,
      @Nullable IParentCmd parent,
      @Nullable Collection<IOption<?>> options)
      throws CmdRuntimeException.CreationFailure {
    super(name, aliases, description, parent, ARGUMENTS, options);
  }

  @SuppressWarnings("unchecked")
  protected IArg<ICmd> getChildrenArgument() {
    return (IArg<ICmd>) getArgument(0);
  }

  protected void checkConflictingChildren(ICmd command) {
    if (children.stream().anyMatch(command::isConflictingWith))
      throw new CmdRuntimeException("tried to add command that conflicts with another child");
  }

  @Override
  public Collection<ICmd> getChildren() {
    return ImmutableList.copyOf(children);
  }

  @Override
  public Collection<ICmd> getChildrenDeep() {
    return children
        .stream()
        .collect(
            Lists::newArrayList,
            (l, e) -> {
              l.add(e);
              if (e instanceof IParentCmd) l.addAll(((IParentCmd) e).getChildrenDeep());
            },
            List::addAll);
  }

  @Override
  public boolean addChild(ICmd command) {
    checkConflictingChildren(command);
    return children.add(command);
  }

  @Override
  public boolean removeChild(ICmd command) {
    return children.remove(command);
  }

  @Override
  public CmdBuilders makeChild() {
    return new CmdBuilders(this);
  }

  @Override
  public ICmd findChild(final String name) {
    return children
        .stream()
        .filter(cmd -> cmd.isNameMatching(name) || cmd.isAbsoluteNameMatching(name))
        .findFirst()
        .orElse(null);
  }

  @Override
  public boolean process(String[] args)
      throws CmdUnknownException, CmdMissingArgumentException, CmdAmbiguousException {
    CmdHelper.requireArgument(args, this, getChildrenArgument());
    Parser<ParentCmd> parser = new Parser<>(this, args);

    @SuppressWarnings("unchecked")
    ArgMap<ICmd> child = (ArgMap<ICmd>) parser.getArgument(0).get();

    List<ICmd> matching = child.values(0, children.stream()).collect(Collectors.toList());

    if (matching.isEmpty()) throw new CmdUnknownException(this, child.getInput(0));
    else if (matching.size() > 1)
      throw new CmdAmbiguousException(this, child.getInput(0), matching);

    return matching.get(0).process(parser.getRemaining());
  }

  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writer.beginObject(); // {

    writer.name("flags");
    CmdFlagSerializer.getInstance().serialize(this, writer);

    writer.name("children");
    CmdChildrenSerializer.getInstance().serialize(this, writer);

    writer.endObject(); // }
  }

  @Override
  public void deserialize(JsonReader reader) throws IOException {
    reader.beginObject(); // {

    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "flags":
          {
            CmdFlagSerializer.getInstance().deserialize(this, reader);
            break;
          }
        case "children":
          {
            CmdChildrenSerializer.getInstance().deserialize(this, reader);
            break;
          }
        default:
          reader.skipValue();
      }
    }

    reader.endObject(); // }
  }
}
