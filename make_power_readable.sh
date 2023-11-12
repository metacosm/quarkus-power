#!/bin/bash

sudo find /sys/class/powercap/**/energy_uj -exec chmod +r {} \;

