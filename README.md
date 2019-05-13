# Kotlin Archiver

A simple tool for working with zip archives from the command line developed just to become a little bit better acquainted with the [Kotlin](https://github.com/JetBrains/kotlin) language.

## Features

- Packing files into one zip file
- Adding files to an existing zip archive
- If a directory is specified instead of an input file, the utility packages its contents recursively
- Unpacking an archive with the ability to specify (existing or not existing) a directory for it
- Archive comment support: reading comments from an archive, adding a comment to an archive, creating an archive with a comment

## Building

Make sure you have installed:

- [Java](http://java.oracle.com/)
- [Maven](http://maven.apache.org/)

Then run the following command in the root directory of the project:

```sh
mvn compile
```

It creates `kotlin-archiver.jar` there.

## Usage

You can run the program in the following way:

```sh
java -jar kotlin-archiver.jar [arguments]
```

Use the following arguments to perform the appropriate action (square brackets mean optional):

```sh
# packing files into the zip archive with the ability to add a comment
-p -z target_file.zip -s source_file_or_dir_1 source_file_or_dir_2 ... [-c "comment"]

# add files into the existing zip archive
-a -z target_file.zip -s source_file_or_dir_1 source_file_or_dir_2 ...

# add an archive comment
-a -z target_file.zip -c "comment"

# extract the archive with the ability to specify destination directory
-e -z archive.zip [-o out_dir]

# get the archive comment
-g -z archive.zip

# help
-h
```

## Dependencies

The following dependencies are used in the project:

- [Kotlin](https://github.com/JetBrains/kotlin) 1.3.31
- [Apache Ant](https://github.com/apache/ant) 1.10.5
- [Apache Commons CLI](https://github.com/apache/commons-cli) 1.4

## License

[MIT](LICENSE) © [alxiw](https://github.com/alxiw)