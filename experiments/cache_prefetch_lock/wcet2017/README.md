# Best Practice for Caching of Single-Path Code

This folder contains information and scripts to run the evaluation experiments
for the paper "Best Practice for Caching of Single-Path Code",
Martin Schoeberl, Bekim Cilku, Daniel Prokesch, and Peter Puschner,
accepted for WCET 2017.

## Prerequisites: T-CREST

We use the open-source platform T-CREST for our experiments. Therefore, you
need all T-CREST tools installed. A brief installation instruction can be found
at the [Patmos repository](https://github.com/t-crest/patmos).

We also provide a [VM with Ubuntu](http://patmos.compute.dtu.dk/)
where all needed packages are installed. However, that VM is used in teaching
and does not contain the latest version of T-CREST. Therefore, you need
to reinstall T-CREST there with:

    rm -rf t-crest
    mkdir ~/t-crest
    cd ~/t-crest
    git clone https://github.com/t-crest/patmos-misc.git misc
    ./misc/build.sh

## The Benchmarks

The benchmarks are executed in folder:

    t-crest/misc/experiments/cache_prefetch_lock/wcet2017

A simple `make` will download the
[TACLe Benchmarks](https://github.com/tacle/tacle-bench)
and compile and run most of them. This will take several hours.
Therefore, for first experiments restrict the number of benchmarks
by setting `ALLAPPS` in the `Makefile` to just a few programs.

The result of the benchmarks are several .txt files containing the
execution time in clock cycles. The naming convention of the text
files is: `<app>_<cache>_<code>` where `<app>` is the benchmark name,
`<cache>` is the cache type (`ic` normal instruction cache, `mc`
method cache, `pc` instruction cache with prefetching, and `lc`
a 2-way set associative instruction cache with LRU replacement), and
`<code>` the compilation type (`np` normal compilation, `sp`
single-path code generation).

### Makefile Targets

`make` will build and run everything.

`make eval` will compute some results and produce tables in PDF form.

`make clean` removes all generated files and folders.

## Repository Tags

You can explore the benchmarks on the head of the repositories.
For reproducibility we provide the tags of the involved repositories:

tacle-bench: fcabf4630cb239f34f37a03ce7d93c563b65c897

patmos: a173b6de37195f7a1096cc8c9a10936ad5f1ca75

llvm: 01543c10ddabcfa635572f3ddfaa01f180dd8a89

compiler-rt: 187888708bd8532789b9a4e24935cf31c70c9024

gold: cdde53177a57f6a7eccb0305089cbbf32c055509

misc: 34d70cd2b929664b060c2238d5e7ad9599996958

## Request for Help

If you encounter any build or reproduction issues you have three options
to notify us (seek for help): (1) open an issue on GitHub, (2) ask on the
Patmos mailing list, or (3) send an email to martin@jopdesign.com.

