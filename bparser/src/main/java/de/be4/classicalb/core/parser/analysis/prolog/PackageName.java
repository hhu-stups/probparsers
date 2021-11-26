package de.be4.classicalb.core.parser.analysis.prolog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import de.be4.classicalb.core.parser.util.Utils;

/**
 * Represents the name of a B machine package.
 * This class simplifies converting package names
 * between their dotted form ({@code "one.two.three"}),
 * a split list form ({@code ["one", "two", "three"]}),
 * and concrete file paths ({@code /root/package/one/two/three}).
 */
public final class PackageName {
	private static final Pattern VALID_IDENTIFIER = Pattern.compile("([\\p{L}][\\p{L}\\p{N}_]*)");
	
	private final List<String> nameParts;
	
	/**
	 * Create a package object from a list of name parts.
	 * In string form,
	 * these name parts are separated by dots.
	 * Use {@link #fromName(String)} to parse such a dotted package name.
	 * 
	 * @param nameParts parts of the package name
	 */
	public PackageName(final List<String> nameParts) {
		this.nameParts = new ArrayList<>(nameParts);
	}
	
	/**
	 * Create a package object from a dotted name.
	 * 
	 * @param name dotted package name
	 * @return package object with the given name
	 * @throws IllegalArgumentException if any part of the package name is not a valid identifier
	 */
	public static PackageName fromName(final String name) {
		final List<String> nameParts = Arrays.asList(name.split("\\."));
		for (final String part : nameParts) {
			if (!VALID_IDENTIFIER.matcher(part).matches()) {
				throw new IllegalArgumentException("Invalid package name " + name + ": name part " + part + " is not a valid package identifier");
			}
		}
		return new PackageName(nameParts);
	}
	
	/**
	 * Create a package object from a dotted name that might be double-quoted.
	 *
	 * @param name possibly quoted dotted package name
	 * @return package object with the given name
	 * @throws IllegalArgumentException if any part of the package name is not a valid identifier
	 */
	public static PackageName fromPossiblyQuotedName(final String name) {
		if (Utils.isQuoted(name, '"')) {
			return fromName(Utils.removeSurroundingQuotes(name, '"'));
		} else {
			return fromName(name);
		}
	}
	
	/**
	 * Get the parts of the package name.
	 * In string form,
	 * these name parts are separated by dots.
	 * Use {@link #getName()} to get such a dotted package name.
	 * 
	 * @return parts of the package name
	 */
	public List<String> getNameParts() {
		return Collections.unmodifiableList(this.nameParts);
	}
	
	/**
	 * Get the dotted name of this package.
	 * 
	 * @return dotted name of this package
	 */
	public String getName() {
		return String.join(".", this.getNameParts());
	}
	
	/**
	 * Get the path of this package based on the given root directory.
	 * 
	 * @param rootDirectory file path of the root package
	 * @return file path of this package based on the root directory
	 */
	public File getFile(final File rootDirectory) {
		File f = rootDirectory;
		for (final String part : this.getNameParts()) {
			f = new File(f, part);
		}
		return f;
	}
	
	/**
	 * Determine the directory of the root package based on this package name and its directory path.
	 * 
	 * @param packageDirectory directory path of this package
	 * @return directory of the root package
	 * @throws IllegalArgumentException if this package name doesn't match the given directory path
	 */
	public File determineRootDirectory(final File packageDirectory) {
		File dir = packageDirectory;
		for (int i = this.getNameParts().size() - 1; i >= 0; i--) {
			final String name1 = this.getNameParts().get(i);
			final String name2 = dir.getName();
			if (!name1.equals(name2)) {
				throw new IllegalArgumentException(String.format(
					"Package declaration '%s' does not match the folder structure: %s vs %s",
					this.getName(), name1, name2
				));
			}
			dir = dir.getParentFile();
		}
		return dir;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final PackageName other = (PackageName)obj;
		return this.getNameParts().equals(other.getNameParts());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getNameParts());
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
}
