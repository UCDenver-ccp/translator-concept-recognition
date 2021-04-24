#!/bin/bash

mkdir /home/dev/iob-output/aggregated

for f in /home/dev/iob-output/train/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/hp.train.ob; done
for f in /home/dev/iob-output/test/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/hp.test.ob; done