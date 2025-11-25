package dev.jsinco.malts.integration;

import dev.jsinco.malts.registry.RegistryCrafter;
import dev.jsinco.malts.utility.Text;

public class IntegrationCrafter implements RegistryCrafter.Extension<Integration> {

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Integration> T craft(Class<?> clazz) {
        try {
            T instance = (T) clazz.getDeclaredConstructor().newInstance();
            if (instance.canRegister()) {
                instance.register();
                Text.log("Registered integration for: " + instance.name());
                return instance;
            } else {
                Text.debug("Skipped registration for integration: " + instance.name());
            }
        } catch (ReflectiveOperationException e) {
            Text.error("Failed to register integration for: " + clazz.getName(), e);
        }
        return null;
    }
}
