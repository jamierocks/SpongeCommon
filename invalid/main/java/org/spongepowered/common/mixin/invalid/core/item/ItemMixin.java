package org.spongepowered.common.mixin.invalid.core.item;

import java.util.List;

@Mixin(Item.class)
public class ItemMixin {

    /**
     * @author gabizou - June 10th, 2019 - 1.12.2
     * @reason This uses the SpongeImplHooks so that we eliminate
     * more Mixins in SpongeForge and SpongeVanilla. The hook here
     * is to allow a mod in Forge to replace a previously registered
     * item. Vanilla doesn't do this, so we don't have to care about
     * vanilla, but Forge, we have to check against the Item registry.
     *
     * @param id The numerical id supposed to be assigned, ignored
     * @param textualID The location id of the item, like "minecraft:diamond_sword"
     * @param itemIn The item instance
     * @param ci callback
     */
    @Inject(method = "registerItem(ILnet/minecraft/util/ResourceLocation;Lnet/minecraft/item/Item;)V", at = @At("RETURN"))
    private static void spongeImpl$registerItemWithSpongeRegistry(final int id, final ResourceLocation textualID, final Item itemIn, final CallbackInfo ci) {
        SpongeImplHooks.registerItemForSpongeRegistry(id, textualID, itemIn);
    }

    @Override
    public void bridge$gatherManipulators(final ItemStack itemStack, final List<Mutable<?, ?>> list) {
        if (!itemStack.hasTag()) {
            return;
        }

        final org.spongepowered.api.item.inventory.ItemStack spongeStack = ((org.spongepowered.api.item.inventory.ItemStack) itemStack);
        if (itemStack.isEnchanted()) {
            list.add(((org.spongepowered.api.item.inventory.ItemStack) itemStack).get(EnchantmentData.class).get());
        }
        spongeStack.get(DisplayNameData.class).ifPresent(list::add);
        spongeStack.get(LoreData.class).ifPresent(list::add);
    }

    @Override
    public ItemStack bridge$createDictionaryStack(final int wildcardValue) {
        return new ItemStack((Item) (Object) this, 1, wildcardValue);
    }

}
