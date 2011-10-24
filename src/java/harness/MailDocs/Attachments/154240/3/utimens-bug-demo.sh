#! /bin/bash
# Copyright (C) 2011 Sebastian Pipping <sebastian@pipping.org>
# Licensed under GPL v2 or later
#
# 2011-06-25 19:15 UTC+2

DO() {
	echo "# $@"
	"$@"
}

DIE() {
	DO "$@" || exit 1
}

NOTE() {
	echo -e "\n### $@:"
}


# Compile examples
DIE ./makeconf.sh
DIE ./configure
DIE make
DIE cd example


# Prepare environment
DIE ln -f -s MISSING symlink
DIE tar cf tarball.tar symlink

DO fusermount -u broken_fusexmp
DIE mkdir -p broken_fusexmp
DIE ./fusexmp broken_fusexmp

DO fusermount -u broken_fusexmp_fh
DIE mkdir -p broken_fusexmp_fh
DIE ./fusexmp_fh broken_fusexmp_fh


# Demo bug
NOTE "Works alright"
DIE rm symlink
DIE tar -xf tarball.tar

NOTE "Broken, follows link on utime"
DIE rm symlink
DO tar -C broken_fusexmp$PWD/ -xf tarball.tar

NOTE "Broken, follows link on utime"
DIE rm symlink
DO tar -C broken_fusexmp_fh$PWD/ -xf tarball.tar
