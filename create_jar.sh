#!/bin/bash
name="Zhang_Li_HW2"
zip -r $name.zip .
cd src/main/java
find . -name *.java > javaFiles
jar -cf $name.jar @javaFiles
mv Zhang_Li_HW2.jar ../../..
rm javaFiles
