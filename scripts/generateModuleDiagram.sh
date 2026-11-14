#!/bin/bash

echo "digraph modules {" | tee /tmp/universalpodcast-modules.dot
modules=$(find . -name "build.gradle")
for modulePath in $modules; do
  name=$(echo $modulePath | sed -E 's/\.\/?(.*)\/build\.gradle/:\1/g' | sed -E 's/\//:/g')
  dependencies=$(cat $modulePath | sed -nE "s/[ ]*implementation project\\(['\"](.*)['\"]\\)/\\1/p")
  for dependency in $dependencies; do
    echo "\"$name\" -> \"$dependency\";" | tee --append /tmp/universalpodcast-modules.dot
  done
done
echo "}" | tee --append /tmp/universalpodcast-modules.dot

tred /tmp/universalpodcast-modules.dot | dot -Tpng > moduleDiagram.png
rm /tmp/universalpodcast-modules.dot
echo "Wrote to moduleDiagram.png"
