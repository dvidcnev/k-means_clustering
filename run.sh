#!/bin/bash
# Whether there should be GUI
while true; do
    echo "Enter DrawMap (true/false):"
    read DrawMap
    if [[ "$DrawMap" == "true" || "$DrawMap" == "false" ]]; then
        break
    else
        echo "Invalid input."
    fi
done
# MODE
while true; do
    echo "Enter mode (0 - Sequential, 1 - Parallel, 2 - Distributive (MPI)):"
    read mode
    if [[  "$mode" -eq 0 || "$mode" -eq 1 || "$mode" -eq 2 ]]; then
        break
    else
        echo "Invalid input."
    fi
done
if [ "$mode" -eq 2 ];
then
    while true; do
        echo "Specify number of processors (e.g., 3):"
        read numProcessors
        if [[ "$numProcessors" -gt 0 ]]; then
            break
        else
            echo "Invalid input."
        fi
    done
fi
while true; do
    echo "Enter NumClusters (e.g., 5):"
    read NumClusters
    if [[ "$NumClusters" -gt 0 ]]; then
        break
    else
        echo "Invalid input."
    fi
done

while true; do
    echo "Enter NumSites (e.g., 1000):"
    read NumSites
    if [[ "$NumSites" -gt 0 ]]; then
        break
    else
        echo "Invalid input."
    fi
done

if [ "$mode" -eq 2 ]; 
then
    mpjrun.sh -np $numProcessors --module-path='/home/david/Desktop/Projects/K-Means-Clustering/javafx' --add-modules='javafx.controls,javafx.fxml,javafx.graphics,javafx.web' --add-exports='javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED' --add-exports='javafx.graphics/com.sun.javafx.util=ALL-UNNAMED' --add-exports='javafx.base/com.sun.javafx.reflect=ALL-UNNAMED' --add-exports='javafx.base/com.sun.javafx.beans=ALL-UNNAMED' --add-exports='javafx.graphics/com.sun.glass.utils=ALL-UNNAMED' --add-exports='javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED' --add-exports='javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED' --add-exports='javafx.web/com.sun.javafx.webkit=ALL-UNNAMED' --add-opens='java.base/java.nio=ALL-UNNAMED' --add-opens='java.base/sun.nio.ch=ALL-UNNAMED' --add-opens='java.base/java.lang=ALL-UNNAMED' --add-opens='java.base/java.lang.invoke=ALL-UNNAMED' --add-opens='java.base/sun.security.action=ALL-UNNAMED' -Dlog4j.configuration=file:src/main/resources/log4j.properties -cp . Scanning -jar target/k-means-clustering-1.jar --DrawMap $DrawMap --mode $mode --NumClusters $NumClusters --NumSites $NumSites

else
    java --module-path='/home/david/Desktop/Projects/K-Means-Clustering/javafx' --add-modules='javafx.controls,javafx.fxml,javafx.graphics,javafx.web' --add-exports='javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED' --add-exports='javafx.graphics/com.sun.javafx.util=ALL-UNNAMED' --add-exports='javafx.base/com.sun.javafx.reflect=ALL-UNNAMED' --add-exports='javafx.base/com.sun.javafx.beans=ALL-UNNAMED' --add-exports='javafx.graphics/com.sun.glass.utils=ALL-UNNAMED' --add-exports='javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED' --add-exports='javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED' --add-exports='javafx.web/com.sun.javafx.webkit=ALL-UNNAMED' --add-opens='java.base/java.nio=ALL-UNNAMED' --add-opens='java.base/sun.nio.ch=ALL-UNNAMED' --add-opens='java.base/java.lang=ALL-UNNAMED' --add-opens='java.base/java.lang.invoke=ALL-UNNAMED' --add-opens='java.base/sun.security.action=ALL-UNNAMED' -Dlog4j.configuration=file:src/main/resources/log4j.propertie -jar target/k-means-clustering-1.jar --DrawMap $DrawMap --mode $mode --NumClusters $NumClusters --NumSites $NumSites
fi
