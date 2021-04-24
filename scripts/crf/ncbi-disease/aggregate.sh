#!/bin/bash

mkdir /home/dev/iob-output/aggregated

for f in /home/dev/iob-output/train/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/ncbidisease.dev.ob; done
for f in /home/dev/iob-output/develop/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/ncbidisease.train.ob; done
for f in /home/dev/iob-output/test/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/ncbidisease.test.ob; done