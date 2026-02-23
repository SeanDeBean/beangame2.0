package com.beangamecore.entities.livingtree;

import com.beangamecore.entities.livingtree.branchgenerators.*;
import com.beangamecore.entities.livingtree.leafgenerators.*;
import com.beangamecore.entities.livingtree.trunkgenerators.*;

public class TreeComponentFactory {
    public BranchGenerator createBranchGenerator(WoodType woodType) {
        switch (woodType) {
            case OAK: return new OakBranchGenerator();
            case SPRUCE: return new SpruceBranchGenerator();
            case BIRCH: return new BirchBranchGenerator();
            case JUNGLE: return new JungleBranchGenerator();
            case ACACIA: return new AcaciaBranchGenerator();
            case DARK_OAK: return new DarkOakBranchGenerator();
            case PALE_OAK: return new PaleOakBranchGenerator();
            case MANGROVE: return new MangroveBranchGenerator();
            case CHERRY: return new CherryBranchGenerator();
            case CRIMSON: return new CrimsonBranchGenerator();
            case WARPED: return new WarpedBranchGenerator();
            default: return new OakBranchGenerator();
        }
    }
    
    public LeafGenerator createLeafGenerator(WoodType woodType) {
        switch (woodType) {
            case OAK: return new OakLeafGenerator();
            case SPRUCE: return new SpruceLeafGenerator();
            case BIRCH: return new BirchLeafGenerator();
            case JUNGLE: return new JungleLeafGenerator();
            case ACACIA: return new AcaciaLeafGenerator();
            case DARK_OAK: return new DarkOakLeafGenerator();
            case PALE_OAK: return new PaleOakLeafGenerator();
            case MANGROVE: return new MangroveLeafGenerator();
            case CHERRY: return new CherryLeafGenerator();
            case CRIMSON: return new CrimsonLeafGenerator();
            case WARPED: return new WarpedLeafGenerator();
            default: return new OakLeafGenerator();
        }
    }
    
    public TrunkGenerator createTrunkGenerator(WoodType woodType) {
        switch (woodType) {
            case DARK_OAK:
            case PALE_OAK:
                return new ThickTrunkGenerator();
            case ACACIA:
                return new AcaciaTrunkGenerator();
            case MANGROVE:
            case CRIMSON:
            case WARPED:
                return new TwistedTrunkGenerator();
            default:
                return new StandardTrunkGenerator();
        }
    }
}
