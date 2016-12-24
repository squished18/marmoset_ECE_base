If you want to define custom ss: tags, create a new directory,
copy your tags there (you can start from the versions in the default
directory), and then set the taglib.custom property in build.properties
to reflect the name of this directory.  Your new directory will
override tags from the default library, so you don't have to
redefine a tag if the default version is appropriate.  See the
"vassar" directory for an example of custom tags.

The "none" directory should remain empty: it is used to specify
that there is no tag customization, and that the default tags should
be used exclusively.
