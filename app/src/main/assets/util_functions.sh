#!/system/bin/sh
#######################################################################################
# Magisk General Utility Functions
# Source: https://github.com/topjohnwu/Magisk (GPL-3.0)
#######################################################################################

###################
# Helper Functions
###################

ui_print() {
  echo "$1"
}

abort() {
  ui_print "$1"
  exit 1
}

grep_prop() {
  local REGEX="s/^$1=//p"
  shift
  local FILES=$@
  [ -z "$FILES" ] && FILES='/system/build.prop'
  cat $FILES 2>/dev/null | dos2unix | sed -n "$REGEX" | head -n 1
}

is_mounted() {
  grep -q " $(readlink -f $1) " /proc/mounts 2>/dev/null
  return $?
}

######################
# Architecture Detection
######################

api_level_arch_detect() {
  API=$(grep_prop ro.build.version.sdk)
  ABI=$(grep_prop ro.product.cpu.abi | cut -c-3)
  ABI2=$(grep_prop ro.product.cpu.abi2 | cut -c-3)
  ABILONG=$(grep_prop ro.product.cpu.abi)

  ARCH=arm
  ARCH32=arm
  IS64BIT=false

  if [ "$ABI" = "x86" ]; then ARCH=x86; ARCH32=x86; fi
  if [ "$ABILONG" = "arm64-v8a" ]; then ARCH=arm64; ARCH32=arm; IS64BIT=true; fi
  if [ "$ABILONG" = "x86_64" ]; then ARCH=x64; ARCH32=x86; IS64BIT=true; fi
}

##############
# ChromeOS signing (placeholder)
##############

sign_chromeos() {
  ui_print "- Signing ChromeOS image"
  echo > empty
  ./chromeos/futility vbutil_kernel --pack new-boot.img.signed \
    --keyblock ./chromeos/kernel.keyblock \
    --signprivate ./chromeos/kernel_data_key.vbprivk \
    --version 1 --vmlinuz new-boot.img \
    --config empty --arch arm --bootloader empty \
    --flags 0x1 2>/dev/null
  rm -f empty
  if [ -f new-boot.img.signed ]; then
    mv new-boot.img.signed new-boot.img
  fi
}
