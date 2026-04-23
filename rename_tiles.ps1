Set-Location 'c:\Jamin Coding\files related to coding\MP-cmsc-12\res\TILES'

# Rename ground tiles
$groundFiles = Get-ChildItem ground*.png | Sort-Object { [int]($_.Name -replace 'ground', '' -replace '.png', '') }
$i = 1
foreach ($file in $groundFiles) {
    $newName = 'ground{0:D3}.png' -f $i
    Rename-Item $file.Name $newName
    $i++
}

# Rename solid tiles
$solidFiles = Get-ChildItem solid*.png | Sort-Object { [int]($_.Name -replace 'solid', '' -replace '.png', '') }
$i = 1
foreach ($file in $solidFiles) {
    $newName = 'solid{0:D3}.png' -f $i
    Rename-Item $file.Name $newName
    $i++
}