package dev.fiki.forgehax.api.key;

import net.minecraft.client.util.InputMappings.Type;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

public interface KeyInputs {
  KeyInput UNBOUND = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.unknown")
      .aliases(Arrays.asList("unknown", "invalid", "none"))
      .code(-1)
      .build();

  KeyInput MOUSE_LEFT = KeyInput.builder()
      .type(Type.MOUSE)
      .name("key.mouse.left")
      .alias("lmouse")
      .code(GLFW.GLFW_MOUSE_BUTTON_LEFT)
      .build();

  KeyInput MOUSE_RIGHT = KeyInput.builder()
      .type(Type.MOUSE)
      .name("key.mouse.right")
      .alias("rmouse")
      .code(GLFW.GLFW_MOUSE_BUTTON_RIGHT)
      .build();

  KeyInput MOUSE_MIDDLE = KeyInput.builder()
      .type(Type.MOUSE)
      .name("key.mouse.middle")
      .alias("mmouse")
      .code(GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
      .build();

  KeyInput MOUSE_4 = KeyInput.builder()
      .type(Type.MOUSE)
      .name("key.mouse.4")
      .code(GLFW.GLFW_MOUSE_BUTTON_4)
      .build();

  KeyInput MOUSE_5 = KeyInput.builder()
      .type(Type.MOUSE)
      .name("key.mouse.5")
      .code(GLFW.GLFW_MOUSE_BUTTON_5)
      .build();

  KeyInput MOUSE_6 = KeyInput.builder()
      .type(Type.MOUSE)
      .name("key.mouse.6")
      .code(GLFW.GLFW_MOUSE_BUTTON_6)
      .build();

  KeyInput MOUSE_7 = KeyInput.builder()
      .type(Type.MOUSE)
      .name("key.mouse.7")
      .code(GLFW.GLFW_MOUSE_BUTTON_7)
      .build();

  KeyInput MOUSE_8 = KeyInput.builder()
      .type(Type.MOUSE)
      .name("key.mouse.8")
      .code(GLFW.GLFW_MOUSE_BUTTON_8)
      .build();

  KeyInput KEY_0 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.0")
      .alias("0")
      .alias("zero")
      .code(GLFW.GLFW_KEY_0)
      .build();

  KeyInput KEY_1 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.1")
      .code(GLFW.GLFW_KEY_1)
      .build();

  KeyInput KEY_2 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.2")
      .code(GLFW.GLFW_KEY_2)
      .build();

  KeyInput KEY_3 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.3")
      .code(GLFW.GLFW_KEY_3)
      .build();

  KeyInput KEY_4 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.4")
      .code(GLFW.GLFW_KEY_4)
      .build();

  KeyInput KEY_5 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.5")
      .code(GLFW.GLFW_KEY_5)
      .build();

  KeyInput KEY_6 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.6")
      .code(GLFW.GLFW_KEY_6)
      .build();

  KeyInput KEY_7 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.7")
      .code(GLFW.GLFW_KEY_7)
      .build();

  KeyInput KEY_8 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.8")
      .code(GLFW.GLFW_KEY_8)
      .build();

  KeyInput KEY_9 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.9")
      .code(GLFW.GLFW_KEY_9)
      .build();

  KeyInput KEY_A = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.a")
      .code(GLFW.GLFW_KEY_A)
      .build();

  KeyInput KEY_B = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.b")
      .code(GLFW.GLFW_KEY_B)
      .build();

  KeyInput KEY_C = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.c")
      .code(GLFW.GLFW_KEY_C)
      .build();

  KeyInput KEY_D = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.d")
      .code(GLFW.GLFW_KEY_D)
      .build();

  KeyInput KEY_E = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.e")
      .code(GLFW.GLFW_KEY_E)
      .build();

  KeyInput KEY_F = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f")
      .code(GLFW.GLFW_KEY_F)
      .build();

  KeyInput KEY_G = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.g")
      .code(GLFW.GLFW_KEY_G)
      .build();

  KeyInput KEY_H = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.h")
      .code(GLFW.GLFW_KEY_H)
      .build();

  KeyInput KEY_I = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.i")
      .code(GLFW.GLFW_KEY_I)
      .build();

  KeyInput KEY_J = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.j")
      .code(GLFW.GLFW_KEY_J)
      .build();

  KeyInput KEY_K = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.k")
      .code(GLFW.GLFW_KEY_K)
      .build();

  KeyInput KEY_L = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.l")
      .code(GLFW.GLFW_KEY_L)
      .build();

  KeyInput KEY_M = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.m")
      .code(GLFW.GLFW_KEY_M)
      .build();

  KeyInput KEY_N = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.n")
      .code(GLFW.GLFW_KEY_N)
      .build();

  KeyInput KEY_O = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.o")
      .code(GLFW.GLFW_KEY_O)
      .build();

  KeyInput KEY_P = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.p")
      .code(GLFW.GLFW_KEY_P)
      .build();

  KeyInput KEY_Q = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.q")
      .code(GLFW.GLFW_KEY_Q)
      .build();

  KeyInput KEY_R = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.r")
      .code(GLFW.GLFW_KEY_R)
      .build();

  KeyInput KEY_S = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.s")
      .code(GLFW.GLFW_KEY_S)
      .build();

  KeyInput KEY_T = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.t")
      .code(GLFW.GLFW_KEY_T)
      .build();

  KeyInput KEY_U = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.u")
      .code(GLFW.GLFW_KEY_U)
      .build();

  KeyInput KEY_V = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.v")
      .code(GLFW.GLFW_KEY_V)
      .build();

  KeyInput KEY_W = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.w")
      .code(GLFW.GLFW_KEY_W)
      .build();

  KeyInput KEY_X = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.x")
      .code(GLFW.GLFW_KEY_X)
      .build();

  KeyInput KEY_Y = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.y")
      .code(GLFW.GLFW_KEY_Y)
      .build();

  KeyInput KEY_Z = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.z")
      .code(GLFW.GLFW_KEY_Z)
      .build();

  KeyInput KEY_F1 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f1")
      .code(GLFW.GLFW_KEY_F1)
      .build();

  KeyInput KEY_F2 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f2")
      .code(GLFW.GLFW_KEY_F2)
      .build();

  KeyInput KEY_F3 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f3")
      .code(GLFW.GLFW_KEY_F3)
      .build();

  KeyInput KEY_F4 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f4")
      .code(GLFW.GLFW_KEY_F4)
      .build();

  KeyInput KEY_F5 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f5")
      .code(GLFW.GLFW_KEY_F5)
      .build();

  KeyInput KEY_F6 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f6")
      .code(GLFW.GLFW_KEY_F6)
      .build();

  KeyInput KEY_F7 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f7")
      .code(GLFW.GLFW_KEY_F7)
      .build();

  KeyInput KEY_F8 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f8")
      .code(GLFW.GLFW_KEY_F8)
      .build();

  KeyInput KEY_F9 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f9")
      .code(GLFW.GLFW_KEY_F9)
      .build();

  KeyInput KEY_F10 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f10")
      .code(GLFW.GLFW_KEY_F10)
      .build();

  KeyInput KEY_F11 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f11")
      .code(GLFW.GLFW_KEY_F11)
      .build();

  KeyInput KEY_F12 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f12")
      .code(GLFW.GLFW_KEY_F12)
      .build();

  KeyInput KEY_F13 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f13")
      .code(GLFW.GLFW_KEY_F13)
      .build();

  KeyInput KEY_F14 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f14")
      .code(GLFW.GLFW_KEY_F14)
      .build();

  KeyInput KEY_F15 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f15")
      .code(GLFW.GLFW_KEY_F15)
      .build();

  KeyInput KEY_F16 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f16")
      .code(GLFW.GLFW_KEY_F16)
      .build();

  KeyInput KEY_F17 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f17")
      .code(GLFW.GLFW_KEY_F17)
      .build();

  KeyInput KEY_F18 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f18")
      .code(GLFW.GLFW_KEY_F18)
      .build();

  KeyInput KEY_F19 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f19")
      .code(GLFW.GLFW_KEY_F19)
      .build();

  KeyInput KEY_F20 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f20")
      .code(GLFW.GLFW_KEY_F20)
      .build();

  KeyInput KEY_F21 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f21")
      .code(GLFW.GLFW_KEY_F21)
      .build();

  KeyInput KEY_F22 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f22")
      .code(GLFW.GLFW_KEY_F22)
      .build();

  KeyInput KEY_F23 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f23")
      .code(GLFW.GLFW_KEY_F23)
      .build();

  KeyInput KEY_F24 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f24")
      .code(GLFW.GLFW_KEY_F24)
      .build();

  KeyInput KEY_F25 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.f25")
      .code(GLFW.GLFW_KEY_F25)
      .build();

  KeyInput KEY_KP_0 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.keypad.0")
      .code(GLFW.GLFW_KEY_KP_0)
      .build();

  KeyInput KEY_KP_1 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.keypad.1")
      .code(GLFW.GLFW_KEY_KP_1)
      .build();

  KeyInput KEY_KP_2 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.keypad.2")
      .code(GLFW.GLFW_KEY_KP_2)
      .build();

  KeyInput KEY_KP_3 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.keypad.3")
      .code(GLFW.GLFW_KEY_KP_3)
      .build();

  KeyInput KEY_KP_4 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.keypad.4")
      .code(GLFW.GLFW_KEY_KP_4)
      .build();

  KeyInput KEY_KP_5 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.keypad.5")
      .code(GLFW.GLFW_KEY_KP_5)
      .build();

  KeyInput KEY_KP_6 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.keypad.6")
      .code(GLFW.GLFW_KEY_KP_6)
      .build();

  KeyInput KEY_KP_7 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.keypad.7")
      .code(GLFW.GLFW_KEY_KP_7)
      .build();

  KeyInput KEY_KP_8 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.keypad.8")
      .code(GLFW.GLFW_KEY_KP_8)
      .build();

  KeyInput KEY_KP_9 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.keypad.9")
      .code(GLFW.GLFW_KEY_KP_9)
      .build();

  KeyInput KEY_KP_ADD = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.keypad.add")
      .code(GLFW.GLFW_KEY_KP_ADD)
      .build();

  KeyInput KEY_KP_DECIMAL = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.keypad.decimal")
      .code(GLFW.GLFW_KEY_KP_DECIMAL)
      .build();

  KeyInput KEY_KP_ENTER = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.keypad.enter")
      .code(GLFW.GLFW_KEY_KP_ENTER)
      .build();

  KeyInput KEY_KP_EQUAL = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.keypad.equal")
      .code(GLFW.GLFW_KEY_KP_EQUAL)
      .build();

  KeyInput KEY_KP_MULTIPLY = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.keypad.multiply")
      .code(GLFW.GLFW_KEY_KP_MULTIPLY)
      .build();

  KeyInput KEY_KP_DIVIDE = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.keypad.divide")
      .code(GLFW.GLFW_KEY_KP_DIVIDE)
      .build();

  KeyInput KEY_KP_SUBTRACT = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.keypad.subtract")
      .code(GLFW.GLFW_KEY_KP_SUBTRACT)
      .build();

  KeyInput KEY_DOWN = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.down")
      .code(GLFW.GLFW_KEY_DOWN)
      .build();

  KeyInput KEY_LEFT = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.left")
      .code(GLFW.GLFW_KEY_LEFT)
      .build();

  KeyInput KEY_RIGHT = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.right")
      .code(GLFW.GLFW_KEY_RIGHT)
      .build();

  KeyInput KEY_UP = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.up")
      .code(GLFW.GLFW_KEY_UP)
      .build();

  KeyInput KEY_APOSTROPHE = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.apostrophe")
      .code(GLFW.GLFW_KEY_APOSTROPHE)
      .build();

  KeyInput KEY_BACKSLASH = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.backslash")
      .code(GLFW.GLFW_KEY_BACKSLASH)
      .build();

  KeyInput KEY_COMMA = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.comma")
      .code(GLFW.GLFW_KEY_COMMA)
      .build();

  KeyInput KEY_EQUAL = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.equal")
      .code(GLFW.GLFW_KEY_EQUAL)
      .build();

  KeyInput KEY_GRAVE_ACCENT = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.grave.accent")
      .code(GLFW.GLFW_KEY_GRAVE_ACCENT)
      .build();

  KeyInput KEY_LEFT_BRACKET = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.left.bracket")
      .code(GLFW.GLFW_KEY_LEFT_BRACKET)
      .build();

  KeyInput KEY_MINUS = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.minus")
      .code(GLFW.GLFW_KEY_MINUS)
      .build();

  KeyInput KEY_PERIOD = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.period")
      .code(GLFW.GLFW_KEY_PERIOD)
      .build();

  KeyInput KEY_RIGHT_BRACKET = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.right.bracket")
      .code(GLFW.GLFW_KEY_RIGHT_BRACKET)
      .build();

  KeyInput KEY_SEMICOLON = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.semicolon")
      .code(GLFW.GLFW_KEY_SEMICOLON)
      .build();

  KeyInput KEY_SLASH = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.slash")
      .code(GLFW.GLFW_KEY_SLASH)
      .build();

  KeyInput KEY_SPACE = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.space")
      .code(GLFW.GLFW_KEY_SPACE)
      .build();

  KeyInput KEY_TAB = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.tab")
      .code(GLFW.GLFW_KEY_TAB)
      .build();

  KeyInput KEY_LEFT_ALT = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.left.alt")
      .code(GLFW.GLFW_KEY_LEFT_ALT)
      .build();

  KeyInput KEY_LEFT_CONTROL = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.left.control")
      .code(GLFW.GLFW_KEY_LEFT_CONTROL)
      .build();

  KeyInput KEY_LEFT_SHIFT = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.left.shift")
      .code(GLFW.GLFW_KEY_LEFT_SHIFT)
      .build();

  KeyInput KEY_LEFT_WIN = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.left.win")
      .code(GLFW.GLFW_KEY_LEFT_SUPER)
      .build();

  KeyInput KEY_RIGHT_ALT = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.right.alt")
      .code(GLFW.GLFW_KEY_RIGHT_ALT)
      .build();

  KeyInput KEY_RIGHT_CONTROL = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.right.control")
      .code(GLFW.GLFW_KEY_RIGHT_CONTROL)
      .build();

  KeyInput KEY_RIGHT_SHIFT = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.right.shift")
      .code(GLFW.GLFW_KEY_RIGHT_SHIFT)
      .build();

  KeyInput KEY_RIGHT_WIN = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.right.win")
      .code(GLFW.GLFW_KEY_RIGHT_SUPER)
      .build();

  KeyInput KEY_ENTER = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.enter")
      .code(GLFW.GLFW_KEY_ENTER)
      .build();

  KeyInput KEY_ESCAPE = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.escape")
      .code(GLFW.GLFW_KEY_ESCAPE)
      .build();

  KeyInput KEY_BACKSPACE = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.backspace")
      .code(GLFW.GLFW_KEY_BACKSPACE)
      .build();

  KeyInput KEY_DELETE = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.delete")
      .code(GLFW.GLFW_KEY_DELETE)
      .build();

  KeyInput KEY_END = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.end")
      .code(GLFW.GLFW_KEY_END)
      .build();

  KeyInput KEY_HOME = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.home")
      .code(GLFW.GLFW_KEY_HOME)
      .build();

  KeyInput KEY_INSERT = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.insert")
      .code(GLFW.GLFW_KEY_INSERT)
      .build();

  KeyInput KEY_PAGE_DOWN = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.page.down")
      .code(GLFW.GLFW_KEY_PAGE_DOWN)
      .build();

  KeyInput KEY_PAGE_UP = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.page.up")
      .code(GLFW.GLFW_KEY_PAGE_UP)
      .build();

  KeyInput KEY_CAPS_LOCK = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.caps.lock")
      .code(GLFW.GLFW_KEY_CAPS_LOCK)
      .build();

  KeyInput KEY_PAUSE = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.pause")
      .code(GLFW.GLFW_KEY_PAUSE)
      .build();

  KeyInput KEY_SCROLL_LOCK = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.scroll.lock")
      .code(GLFW.GLFW_KEY_SCROLL_LOCK)
      .build();

  KeyInput KEY_MENU = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.menu")
      .code(GLFW.GLFW_KEY_MENU)
      .build();

  KeyInput KEY_PRINT_SCREEN = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.print.screen")
      .code(GLFW.GLFW_KEY_PRINT_SCREEN)
      .build();

  KeyInput KEY_WORLD_1 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.world.1")
      .code(GLFW.GLFW_KEY_WORLD_1)
      .build();

  KeyInput KEY_WORLD_2 = KeyInput.builder()
      .type(Type.KEYSYM)
      .name("key.keyboard.world.2")
      .code(GLFW.GLFW_KEY_WORLD_2)
      .build();
}
