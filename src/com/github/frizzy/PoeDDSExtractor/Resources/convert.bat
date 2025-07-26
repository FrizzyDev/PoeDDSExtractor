cd /D "%~dp0"
for %%f in (*.dds) do (
    echo %%~nf
    texconv %%f -srgb -ft png -f R8G8B8A8_UNORM_SRGB -y
)
:: -srgb preserves color accuracy
:: This bat file will convert all .dds files in its local directory.