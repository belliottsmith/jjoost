package org.jjoost.util.filters ;

import java.io.File ;
import java.io.FileFilter ;
import java.util.regex.Pattern ;

import org.jjoost.util.Filter ;

public class FilterFileNamePattern implements FileFilter, Filter<File> {

	private static final long serialVersionUID = 2312285149003933324L ;

	private final boolean matchFullPath ;
	private final Pattern pattern ;

	public FilterFileNamePattern(Pattern pattern, boolean matchFullPath) {
		this.pattern = pattern ;
		this.matchFullPath = matchFullPath ;
	}

	public FilterFileNamePattern(String pattern, boolean matchFullPath) {
		this.pattern = Pattern.compile(pattern) ;
		this.matchFullPath = matchFullPath ;
	}

	public boolean accept(File test) {
		return pattern.matcher(matchFullPath ? test.getAbsolutePath() : test.getName()).matches() ;
	}

	public String toString() {
		return (matchFullPath ? "file name " : "file path ") + "matches pattern \"" + pattern.toString() + "\"" ;
	}

}
