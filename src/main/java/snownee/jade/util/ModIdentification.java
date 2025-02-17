package snownee.jade.util;

import java.util.Map;
import java.util.Optional;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.callback.JadeItemModNameCallback;
import snownee.jade.impl.WailaClientRegistration;

public class ModIdentification implements ResourceManagerReloadListener {

	public static final ModIdentification INSTANCE = new ModIdentification();
	private static final Map<String, Optional<String>> NAMES = Maps.newConcurrentMap();

	public static void invalidateCache() {
		NAMES.clear();
	}

	public static Optional<String> getModName(String namespace) {
		return NAMES.computeIfAbsent(namespace, $ -> {
			Optional<String> optional = ClientProxy.getModName($);
			if (optional.isPresent()) {
				return optional;
			}
			String key = "jade.modName." + $;
			if (I18n.exists(key)) {
				return Optional.of(I18n.get(key));
			} else {
				return Optional.empty();
			}
		});
	}

	public static String getModName(ResourceLocation id) {
		return getModName(id.getNamespace()).orElse(id.getNamespace());
	}

	public static String getModName(Block block) {
		return getModName(CommonProxy.getId(block));
	}

	public static String getModName(ItemStack stack) {
		for (JadeItemModNameCallback callback : WailaClientRegistration.instance().itemModNameCallback.callbacks()) {
			String s = callback.gatherItemModName(stack);
			if (!Strings.isNullOrEmpty(s)) {
				return s;
			}
		}
		String id = CommonProxy.getModIdFromItem(stack);
		return getModName(id).orElse(id);
	}

	public static String getModName(Entity entity) {
		if (entity instanceof Painting painting) {
			return getModName(painting.getVariant().unwrapKey().orElseThrow().location());
		}
		if (entity instanceof ItemEntity itemEntity) {
			return getModName(itemEntity.getItem());
		}
		if (entity instanceof FallingBlockEntity fallingBlock) {
			return getModName(fallingBlock.getBlockState().getBlock());
		}
		return getModName(CommonProxy.getId(entity.getType()));
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		invalidateCache();
	}

}
