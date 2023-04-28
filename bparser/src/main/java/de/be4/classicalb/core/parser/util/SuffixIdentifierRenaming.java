package de.be4.classicalb.core.parser.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Adds a numerical suffix to identifiers that conflict with B keywords.
 * It does <i>not</i> handle other identifiers that are not valid B syntax, e. g. containing symbols.
 */
public final class SuffixIdentifierRenaming implements IIdentifierRenaming {
	private final Map<String, String> renamingTable;
	
	public SuffixIdentifierRenaming() {
		super();
		
		this.renamingTable = new HashMap<>();
	}
	
	@Override
	public String renameIdentifier(final String identifier) {
		return this.renamingTable.computeIfAbsent(identifier, id -> {
			if (Utils.isPlainBIdentifier(id) && !this.renamingTable.containsValue(id)) {
				return id;
			} else {
				int i = 1;
				String renamed;
				do {
					renamed = id + "_" + i;
					i++;
				} while (renamingTable.containsValue(renamed));
				return renamed;
			}
		});
	}
}
