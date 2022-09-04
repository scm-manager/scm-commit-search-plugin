#! /usr/bin/bash

# A---B---C---D  master
#      \
#       E        develop
#        \
#         F---G  feature

# $1 = SCM-Manager instance url
# $2 = Output directory
# $3 = Namespace
# $4 = Name

mkdir "$2/$4"
cd "$2/$4" || exit
git init -b main

# A
"I am an animal" > Antelope.txt
git add Antelope.txt
git commit -m "Release the Antelope"

# B
"I am rock solid" > Boulder.txt
git add Boulder.txt
git commit -m "Lift a Boulder"

# Develop
git checkout -b develop
git checkout main

# C
"Am I a cat, am I a fish ? Who knows!" > Catfish.txt
git add Catfish.txt
git commit -m "Actually, a Catfish is a fish"

# D
"Which dollar am I, Canadian, US, Australian ?" > Dollar.txt
git add Dollar.txt
git commit -m "A Dollar is what I need"

# E
git checkout develop
"We even have adjectives!" > Elegant.txt
git add Elegant.txt
git commit -m "An elegant panda is counting to 42"

# Feature
git checkout -b feature

# F
"I like this name" > Fridolin.txt
git add Fridolin.txt
git commit -m "Fridolin likes the SCM-Manager, be more like Fridolin"

# G
"Yaarrgh! I am a pirate" > Grog.txt
git add Grog.txt
git commit -m "Pirates drink Grog"

# Push
git remote add origin "$1/repo/$3/$4"
git push --all origin

# Example B
git push -d origin feature
