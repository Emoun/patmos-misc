
# Experiments for WCET 2017 paper on caching for single-path code
#
# This must be in a subfolder to make the Python scripts happy


###############################################################################
# Common configuration
#

SHELL = /bin/bash

# to override default paths
-include config.mk

# default paths
T-CREST      ?= ../../../..
PATMOS_HWDIR  = $(T-CREST)/patmos/hardware
TACLE         = ./tacle-bench
TACLE_LOCAL   = ./tacle-src-local

# cache size / burst length setup
XML_SIZE ?= 8192
XML_BURSTLENGTH ?= 4


# kernel benchmarks
# binarysearch	countnegative	iir		matrix1		quicksort
# bitcount	fac		insertsort	md5		recursion
# bitonic		fft		jfdctint	minver		sha
# bsort		filterbank	lms		pm		st
# complex_updates	fir2dim		ludcmp		prime

# bitcount, fft gives exit value of 1, broken on Patmos?
# bsort, ludcmp are faster with SP, why?
#   because I missed disable-inlining
# minver mit SP code gibt exit != 0


APP?=ludcmp

# those are all benchmarks (except parallel) from TACLeBench
# Which are useful? Which are broken due to byte order? Which cannot be transformed to SP code?
ALLALL=adpcm_dec adpcm_enc ammunition anagram audiobeam binarysearch bitcount bitonic bsort cjpeg_transupp cjpeg_wrbmp complex_updates countnegative cover dijkstra duff epic fac fft filterbank fir2dim fmref g723_enc gsm_dec gsm_encode h264_dec huff_dec huff_enc iir insertsort jfdctint kernells lift lms ludcmp matrix1 md5 minver mpeg2 ndes petrinet pm powerwindow prime quicksort recursion rijndael_dec rijndael_enc sha st statemate susan test3

# Issues: ammunition does not compile
# anagram, bitcount endless loop
# bitonic as SP endless loop
# simply too long: cjpeg_transupp, dijkstra, epic, filterbank, huff_enc, mpeg2, pm, quicksort, recursion, rijndael_dec, rijndael_enc, test3
# compile error: susan

ALLALL=adpcm_dec adpcm_enc audiobeam binarysearch bsort cjpeg_wrbmp complex_updates countnegative cover duff fac fft fir2dim fmref g723_enc gsm_dec gsm_encode h264_dec huff_dec iir insertsort jfdctint kernells lift lms ludcmp matrix1 md5 minver  ndes petrinet powerwindow prime sha st statemate

# TODO: find a small set of benchmarks that execute relative quick
# for playing with the scripts
#ALLAPPS?=binarysearch bsort
ALLAPPS?=$(ALLALL)

SPMAIN=main
#SPMAIN=$(APP)_main
SPM=-mpatmos-singlepath=$(SPMAIN)

# -mpatmos-disable-function-splitter as not needed in a normal I$
# -mpatmos-method-cache-size=8192 maybe not

CFLAGS = -O2 -g -Xllc -mpatmos-disable-vliw=false
# -Xllc -mpatmos-subfunction-align=32
# -Xopt -disable-inlining


.PHONY: all doall comp doit clean hello tacle-local

all: doall

FORCE:

hello:
	echo "Hello World"
	ls $(TACLE)

###############################################################################
# Get a local copy of the tacle-bench
#

tacle-local: $(TACLE_LOCAL)

$(TACLE):
	-git clone https://github.com/tacle/tacle-bench.git $@

# create a local copy of the tacle source directory and
# mark all main() as noinline
$(TACLE_LOCAL): $(TACLE)
	mkdir -p $@
	cp -r $</bench/app/* $@
	cp -r $</bench/kernel/* $@
	cp -r $</bench/sequential/* $@
	cp -r $</bench/test/* $@
	./modmain.sh



###############################################################################
# Hardware/Emulator
#
patmos_emu_prefetch: XML_TYPE = prefetch
patmos_emu_prefetch: XML_ASSOC = 1
patmos_emu_prefetch: $(APP).rpt pcache.xml
	cd $(T-CREST)/patmos && git checkout icache_with_prefetcher
	cp pcache.xml $(PATMOS_HWDIR)/config/
	cp $< $(PATMOS_HWDIR)/src/icache/rpt.txt
	$(MAKE) -C $(T-CREST)/patmos clean emulator BOARD=pcache
	cp $(PATMOS_HWDIR)/build/emulator $@


patmos_emu_mc: XML_TYPE = method
patmos_emu_mc: XML_ASSOC = 32
patmos_emu_mc: mcache.xml
	cd $(T-CREST)/patmos && git checkout b99441ede979f4c
	cp mcache.xml $(PATMOS_HWDIR)/config/
	$(MAKE) -C $(T-CREST)/patmos clean emulator BOARD=mcache
	cp $(PATMOS_HWDIR)/build/emulator $@


patmos_emu_ic: XML_TYPE = line
patmos_emu_ic: XML_ASSOC = 1
patmos_emu_ic: icache.xml
	cd $(T-CREST)/patmos && git checkout b99441ede979f4c
	cp icache.xml $(PATMOS_HWDIR)/config/
	$(MAKE) -C $(T-CREST)/patmos clean emulator BOARD=icache
	cp $(PATMOS_HWDIR)/build/emulator $@




%cache.xml: template.xml
	sed \
	  -e "s/XML_BURSTLENGTH/$(XML_BURSTLENGTH)/" \
	  -e "s/XML_TYPE/$(XML_TYPE)/" \
	  -e "s/XML_SIZE/$(XML_SIZE)/" \
	  -e "s/XML_ASSOC/$(XML_ASSOC)/" \
	  $< > $@


###############################################################################
# Compilation
#

$(APP).nofs.sp.elf: $(TACLE_LOCAL)
	patmos-clang $(CFLAGS) $(TACLE_LOCAL)/$(APP)/*.c -o $@ \
	  -mpatmos-disable-function-splitter \
	  -mpatmos-singlepath=$(SPMAIN)

$(APP).sp.elf: $(TACLE_LOCAL)
	patmos-clang $(CFLAGS) $(TACLE_LOCAL)/$(APP)/*.c -o $@ \
	  -mpatmos-max-subfunction-size=4096 -mpatmos-preferred-subfunction-size=1024 \
	  -mpatmos-singlepath=$(SPMAIN)

$(APP).nofs.elf: $(TACLE_LOCAL)
	patmos-clang $(CFLAGS) $(TACLE_LOCAL)/$(APP)/*.c -o $@ \
	  -mpatmos-disable-function-splitter

$(APP).elf: $(TACLE_LOCAL)
	patmos-clang $(CFLAGS) $(TACLE_LOCAL)/$(APP)/*.c -o $@ \
	  -mpatmos-max-subfunction-size=4096 -mpatmos-preferred-subfunction-size=1024

###############################################################################
# Generation of the prefetch table
#

# command to generate an instruction trace (pasim is faster)
TRACE_CMD = pasim -G0 -m8M --gsize=32M --debug=0 --debug-fmt=trace \
	    $< 2>&1 >/dev/null | cut -d' ' -f1
#TRACE_CMD = patmos-emulator -r $< | awk '{ printf("%08x\n", $1) }'

%.rpt: %.sym %.sptrace
	python2 ../mktables.py --rpt -e \
	  --size=$(shell dc -e "$(XML_BURSTLENGTH) 4 *p") \
	  --line=$(shell dc -e "$(XML_SIZE) $(XML_BURSTLENGTH) /4/p") \
	  $^ > $@


# Extract the start addresses of the functions from the symbol table
# (required for trace analysis)
%.sym: %.nofs.sp.elf
	patmos-llvm-objdump -t $< | grep -E "^[0-9a-f]{8} [gl]\s+F\s+.text" | \
	  awk '{print $$1, $$6}' | sort > $@


# Generate a compressed instruction address trace
%.trace.gz: %.nofs.sp.elf
	$(TRACE_CMD) | gzip > $@


# extract the subtraces from a compressed, complete trace
%.sptrace: %.sym %.trace.gz
	../extract_subtraces.py $*.sym <(gunzip -c $*.trace.gz) $(SPMAIN)
	mv main.trace $@


###############################################################################

.PHONY: report


report: $(APP)_pc_sp.txt \
  	$(APP)_ic_sp.txt $(APP)_ic_np.txt \
  	$(APP)_mc_sp.txt $(APP)_mc_np.txt
	cd $(T-CREST)/patmos; git checkout master

# target-specific variables
$(APP)_pc_sp.txt: LABEL = pcache sp
$(APP)_pc_sp.txt: $(APP).nofs.sp.elf  patmos_emu_prefetch
	echo "$(APP) $(LABEL)" >> $@
	./patmos_emu_prefetch -p $< | grep exec_cycles >> $@


$(APP)_mc_sp.txt: LABEL = mcache sp
$(APP)_mc_sp.txt: $(APP).sp.elf  patmos_emu_mc
	echo "$(APP) $(LABEL)" >> $@
	./patmos_emu_mc -p $< | grep exec_cycles >> $@


$(APP)_ic_sp.txt: LABEL = icache sp
$(APP)_ic_sp.txt: $(APP).nofs.sp.elf  patmos_emu_ic
	echo "$(APP) $(LABEL)" >> $@
	./patmos_emu_ic -p $< | grep exec_cycles >> $@


$(APP)_mc_np.txt: LABEL = mcache np
$(APP)_mc_np.txt: $(APP).elf  patmos_emu_mc
	echo "$(APP) $(LABEL)" >> $@
	./patmos_emu_mc -p $< | grep exec_cycles >> $@


$(APP)_ic_np.txt: LABEL = icache np
$(APP)_ic_np.txt: $(APP).nofs.elf  patmos_emu_ic
	echo "$(APP) $(LABEL)" >> $@
	./patmos_emu_ic -p $< | grep exec_cycles >> $@

###############################################################################


doall: $(TACLE_LOCAL)
	for P in $(ALLAPPS); do $(MAKE) report APP=$${P}; done;

eval:
	scala eval.scala > data.dat

latex:
	pdflatex -shell-escape genfig.tex
	mv genfig-figure0.pdf example.pdf

clean:
	rm -f *.txt *.elf *.dpth *.log *.md5 *.pdf *.auxlock *.aux *.dat *.rpt *.sym
	rm -f *cache.xml
	rm -f patmos_emu_*
	rm -rf $(TACLE_LOCAL)
	rm -rf $(TACLE)

PAPER?=$(HOME)/paper/tcapapers/2017/spcaching

ms_copy:
	cp *.pdf $(PAPER)


