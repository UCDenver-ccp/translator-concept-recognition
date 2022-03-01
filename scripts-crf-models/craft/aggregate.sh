#!/bin/bash

mkdir /home/dev/iob-output/aggregated

for f in /home/dev/iob-output/dev/CHEBI/CHEBI+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/chebi.dev.ob; done
for f in /home/dev/iob-output/train/CHEBI/CHEBI+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/chebi.train.ob; done
for f in /home/dev/iob-output/test/CHEBI/CHEBI+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/chebi.test.ob; done

for f in /home/dev/iob-output/dev/CL/CL+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/cl.dev.ob; done
for f in /home/dev/iob-output/train/CL/CL+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/cl.train.ob; done
for f in /home/dev/iob-output/test/CL/CL+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/cl.test.ob; done

for f in /home/dev/iob-output/dev/GO_BP/GO_BP+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/go_bp.dev.ob; done
for f in /home/dev/iob-output/train/GO_BP/GO_BP+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/go_bp.train.ob; done
for f in /home/dev/iob-output/test/GO_BP/GO_BP+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/go_bp.test.ob; done

for f in /home/dev/iob-output/dev/GO_CC/GO_CC+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/go_cc.dev.ob; done
for f in /home/dev/iob-output/train/GO_CC/GO_CC+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/go_cc.train.ob; done
for f in /home/dev/iob-output/test/GO_CC/GO_CC+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/go_cc.test.ob; done

for f in /home/dev/iob-output/dev/GO_MF/GO_MF+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/go_mf.dev.ob; done
for f in /home/dev/iob-output/train/GO_MF/GO_MF+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/go_mf.train.ob; done
for f in /home/dev/iob-output/test/GO_MF/GO_MF+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/go_mf.test.ob; done

for f in /home/dev/iob-output/dev/MOP/MOP+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/mop.dev.ob; done
for f in /home/dev/iob-output/train/MOP/MOP+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/mop.train.ob; done
for f in /home/dev/iob-output/test/MOP/MOP+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/mop.test.ob; done

for f in /home/dev/iob-output/dev/NCBITaxon/NCBITaxon+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/ncbitaxon.dev.ob; done
for f in /home/dev/iob-output/train/NCBITaxon/NCBITaxon+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/ncbitaxon.train.ob; done
for f in /home/dev/iob-output/test/NCBITaxon/NCBITaxon+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/ncbitaxon.test.ob; done

for f in /home/dev/iob-output/dev/PR/PR+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/pr.dev.ob; done
for f in /home/dev/iob-output/train/PR/PR+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/pr.train.ob; done
for f in /home/dev/iob-output/test/PR/PR+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/pr.test.ob; done

for f in /home/dev/iob-output/dev/SO/SO+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/so.dev.ob; done
for f in /home/dev/iob-output/train/SO/SO+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/so.train.ob; done
for f in /home/dev/iob-output/test/SO/SO+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/so.test.ob; done

for f in /home/dev/iob-output/dev/UBERON/UBERON+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/uberon.dev.ob; done
for f in /home/dev/iob-output/train/UBERON/UBERON+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/uberon.train.ob; done
for f in /home/dev/iob-output/test/UBERON/UBERON+extensions/ob/*.ob; do (cat "${f}"; echo) >> /home/dev/iob-output/aggregated/uberon.test.ob; done
