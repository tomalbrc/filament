package de.tomalbrc.filament.decoration;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.lib.flowpowered.math.vector.Vector3i;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class GriefDefenderSupport {
    public static boolean isAdminClaim(Level level, BlockPos blockPos) {
        final Claim claim = GriefDefender.getCore().getClaimManager(GriefDefender.getCore().getWorldUniqueId(level)).getClaimAt(new Vector3i(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
        return claim != null && claim.isAdminClaim();
    }
}
