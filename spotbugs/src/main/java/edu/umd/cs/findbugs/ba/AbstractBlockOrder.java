/*
 * Bytecode Analysis Framework
 * Copyright (C) 2003,2004 University of Maryland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.umd.cs.findbugs.ba;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Abstract base class for BlockOrder variants. It allows the subclass to
 * specify just a Comparator for BasicBlocks, and handles the work of doing the
 * sorting and providing Iterators.
 *
 * @see BlockOrder
 */
public abstract class AbstractBlockOrder implements BlockOrder {
    private final ArrayList<BasicBlock> blockList;
    private final Comparator<BasicBlock> comparator;

    protected AbstractBlockOrder(CFG cfg, Comparator<BasicBlock> comparator) {
        this.comparator = comparator;

        // Put the blocks in an array
        int numBlocks = cfg.getNumBasicBlocks();
        blockList = new ArrayList<>(cfg.getNumBasicBlocks());
        for (BasicBlock bb : cfg.blocks()) {
            blockList.add(bb);
        }
        assert blockList.size() == numBlocks;

        // Sort the blocks according to the comparator
        Collections.sort(blockList, comparator);
    }

    @Override
    public Iterator<BasicBlock> blockIterator() {
        return blockList.iterator();
    }

    @Override
    public int compare(BasicBlock b1, BasicBlock b2) {
        return comparator.compare(b1, b2);
    }
}
