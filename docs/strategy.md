# Deployment Strategy

* Maven Type
    * maven:assembly: Use "assembly:assembly" to package.
    * maven:install: Execute "mvn install".
    * maven:package: Use "package" to package (also used for creating dependency packages with the shade plugin).
    * maven:assembly:archive: Use "assembly:single" with assembly.xml to generate a tar.gz package.

* Script Type
    * code: Scripting languages or non-compiled languages.
