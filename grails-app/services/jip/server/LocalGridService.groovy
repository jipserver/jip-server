package jip.server

import jip.grid.local.LocalGrid

/**
 * Manage local grids
 */
class LocalGridService {

    Map<String, LocalGrid> grids = [:]

    void register(String name, Map config){
        if(grids.containsKey(name)) return
        grids[name] = new LocalGrid(name, config);
    }

    LocalGrid get(String name){
        return grids[name]
    }
}
