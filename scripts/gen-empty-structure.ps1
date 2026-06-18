#Requires -Version 5.1
<#
.SYNOPSIS
    Generates a minimal empty structure NBT file (gzip-compressed) for use as a
    NeoForge/Forge GameTest template.

.PARAMETER DataVersion
    Minecraft data version integer. For an empty structure (no blocks, no entities)
    this value is ignored by DFU, so any integer works. Defaults to 0.

.PARAMETER Out
    Output path for the .nbt file.
#>

param(
    [int]    $DataVersion = 0,
    [string] $Out = "armor_stand_processor_test_empty.nbt"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Be-BigEndian-Int16([int]$n) {
    [byte[]]@(($n -shr 8) -band 0xFF, $n -band 0xFF)
}

function Be-BigEndian-Int32([int]$n) {
    [byte[]]@(
        ($n -shr 24) -band 0xFF,
        ($n -shr 16) -band 0xFF,
        ($n -shr 8)  -band 0xFF,
         $n          -band 0xFF
    )
}

function Nbt-String([string]$s) {
    $b = [System.Text.Encoding]::UTF8.GetBytes($s)
    (Be-BigEndian-Int16 $b.Length) + $b
}

function Tag-Int([string]$name, [int]$value) {
    [byte[]]@(0x03) + (Nbt-String $name) + (Be-BigEndian-Int32 $value)
}

function Tag-List-Int([string]$name, [int[]]$values) {
    [byte[]]@(0x09) + (Nbt-String $name) + [byte[]]@(0x03) + (Be-BigEndian-Int32 $values.Length) +
    ($values | ForEach-Object { Be-BigEndian-Int32 $_ })
}

function Tag-List-Compound-Empty([string]$name) {
    [byte[]]@(0x09) + (Nbt-String $name) + [byte[]]@(0x0A) + (Be-BigEndian-Int32 0)
}

# Build raw NBT
$raw = [byte[]]@(0x0A, 0x00, 0x00) +          # TAG_Compound, name ""
    (Tag-Int "DataVersion" $DataVersion) +
    (Tag-List-Int "size" @(1, 1, 1)) +
    (Tag-List-Compound-Empty "palette") +
    (Tag-List-Compound-Empty "blocks") +
    (Tag-List-Compound-Empty "entities") +
    [byte[]]@(0x00)                             # TAG_End

# Gzip compress
$ms  = New-Object System.IO.MemoryStream
$gz  = New-Object System.IO.Compression.GZipStream($ms, [System.IO.Compression.CompressionMode]::Compress)
$gz.Write($raw, 0, $raw.Length)
$gz.Close()
$compressed = $ms.ToArray()

$OutPath = if ([System.IO.Path]::IsPathRooted($Out)) { $Out } else {
    Join-Path (Get-Location) $Out
}

[System.IO.File]::WriteAllBytes($OutPath, $compressed)
Write-Host "Written $($compressed.Length) bytes to $OutPath"
