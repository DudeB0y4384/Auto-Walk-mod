package com.example.autowalk;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.v1.client.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Mod cliente: al presionar la tecla asignada, el jugador camina
 * hacia adelante automáticamente hasta que:
 *  - se vuelva a presionar la misma tecla, o
 *  - el jugador presione W, A, S o D de verdad.
 */
public class AutoWalkClient implements ClientModInitializer {

	// Categoría de la keybind (formato requerido desde 1.21.9+)
	private static final KeyBinding.Category CATEGORY =
			KeyBinding.Category.create(Identifier.of("autowalk", "main"));

	// La tecla que activa/desactiva el auto-caminar.
	// Por defecto: tecla "G" (GLFW_KEY_G). El jugador puede
	// reasignarla en Opciones > Controles como cualquier otra keybind.
	private static final KeyBinding TOGGLE_KEY = new KeyBinding(
			"key.autowalk.toggle",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_G,
			CATEGORY
	);

	// Estado interno
	private boolean autoWalking = false;
	// Recuerda si fuimos NOSOTROS quienes marcamos forwardKey como presionada,
	// para no interferir con una pulsación real del jugador.
	private boolean weForcedForward = false;

	@Override
	public void onInitializeClient() {
		KeyBindingHelper.registerKeyBinding(TOGGLE_KEY);

		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
	}

	private void onClientTick(MinecraftClient client) {
		if (client.player == null) {
			// Si no hay jugador (p. ej. en el menú principal), aseguramos
			// que no dejemos nada "atascado" y salimos.
			autoWalking = false;
			weForcedForward = false;
			return;
		}

		// Consumir pulsaciones de la tecla de toggle
		while (TOGGLE_KEY.wasPressed()) {
			setAutoWalking(client, !autoWalking);
		}

		if (autoWalking) {
			if (isRealMovementKeyPressed(client)) {
				// El jugador se movió manualmente: cancelamos el auto-caminar.
				setAutoWalking(client, false);
			} else {
				// Simulamos que W está presionada.
				client.options.forwardKey.setPressed(true);
				weForcedForward = true;
			}
		}
	}

	private void setAutoWalking(MinecraftClient client, boolean value) {
		autoWalking = value;
		if (!autoWalking && weForcedForward) {
			// Soltamos la tecla forward que habíamos forzado nosotros.
			client.options.forwardKey.setPressed(false);
			weForcedForward = false;
		}
	}

	/**
	 * Comprueba el estado REAL del teclado (no las keybindings, que
	 * podríamos haber alterado nosotros) para W, A, S y D.
	 */
	private boolean isRealMovementKeyPressed(MinecraftClient client) {
		long windowHandle = client.getWindow().getHandle();
		return InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_W)
				|| InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_A)
				|| InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_S)
				|| InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_D);
	}
}
