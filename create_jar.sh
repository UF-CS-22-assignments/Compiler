#!/bin/bash
name="Zhang_Li_HW5"
zip -r $name.zip . -x ./target/*
cd src/main/java
find . -name *.java > javaFiles
jar -cf $name.jar @javaFiles
mv $name.jar ../../..
rm javaFiles
