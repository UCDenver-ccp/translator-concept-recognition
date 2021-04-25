#!/bin/bash

mkdir /home/dev/iob-output/aggregated

for f in /home/dev/iob-output/train/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/nlmchem.train.ob; done
for f in /home/dev/iob-output/dev/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/nlmchem.dev.ob; done
for f in /home/dev/iob-output/test/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/nlmchem.test.ob; done