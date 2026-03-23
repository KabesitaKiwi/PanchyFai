$homePath = 'c:\Users\ellagoss\AndroidStudioProjects\PanchyFai\app\src\main\java\com\example\panchify\vistas\Home.kt'
$text = [IO.File]::ReadAllText($homePath)
$text = $text -replace '\}\s*\}\s*\}\s*override fun onResume', '} `n    } `n`n    override fun onResume'
[IO.File]::WriteAllText($homePath, $text)
Write-Host 'Fixed Home.kt'

$songsPath = 'c:\Users\ellagoss\AndroidStudioProjects\PanchyFai\app\src\main\java\com\example\panchify\vistas\Songs.kt'
$text2 = [IO.File]::ReadAllText($songsPath)
$text2 = $text2 -replace '\}\)\s*\}\)\s*\}\s*override fun onResume', '}) `n    } `n`n    override fun onResume'
[IO.File]::WriteAllText($songsPath, $text2)
Write-Host 'Fixed Songs.kt'
