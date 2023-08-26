package betterbeacons.mixins;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin
{
  private static float[] count = new float[]{0, 0, 0, 0};
  private static float finalcount;
  private static int layer;

  @Inject(method = "updateBase", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
  private static void captureLayer(Level level, int x, int y, int z, CallbackInfoReturnable<Integer> cir, int l, int m)
  {
    layer = m - 1;
  }

  @Redirect(method = "updateBase", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"))
  private static boolean is(BlockState state, TagKey<Block> tag)
  {
    count[layer] +=
      state.is(Blocks.IRON_BLOCK) ? 1 :
      state.is(Blocks.GOLD_BLOCK) ? 1.7 :
      state.is(Blocks.EMERALD_BLOCK) ? 2.4 :
      state.is(Blocks.DIAMOND_BLOCK) ? 3.7 :
      state.is(Blocks.NETHERITE_BLOCK) ? 5 :
      0;
    return state.is(tag);
  }

  @Inject(method = "updateBase", at = @At("RETURN"))
  private static void onReturn(Level level, int x, int y, int z, CallbackInfoReturnable<Integer> cir)
  {
    finalcount = 0;
    int l = cir.getReturnValue();
    for(int i = 0; i < l; i ++)
      finalcount += count[i];
    count = new float[]{0, 0, 0, 0};
    finalcount /=
      l == 1 ? 9 :
      l == 2 ? 34 :
      l == 3 ? 83 :
      l == 4 ? 164 :
      1;
  }

  @Redirect(method = "applyEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;"))
  private static AABB modifyLevels(AABB aabb, double original)
  {
    return aabb.inflate(original * finalcount);
  }
}
