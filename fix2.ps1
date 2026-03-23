$homePath = 'c:\Users\ellagoss\AndroidStudioProjects\PanchyFai\app\src\main\java\com\example\panchify\vistas\Home.kt'
$text = [IO.File]::ReadAllText($homePath)
$bad = '} `n    } `n`n    override fun onResume'
$good = '}' + [Environment]::NewLine + '    }' + [Environment]::NewLine + [Environment]::NewLine + '    override fun onResume'
$text = $text.Replace($bad, $good)
[IO.File]::WriteAllText($homePath, $text)

$songsPath = 'c:\Users\ellagoss\AndroidStudioProjects\PanchyFai\app\src\main\java\com\example\panchify\vistas\Songs.kt'
$text2 = [IO.File]::ReadAllText($songsPath)
$bad2 = '}) `n    } `n`n    override fun onResume'
$good2 = '})' + [Environment]::NewLine + '    }' + [Environment]::NewLine + [Environment]::NewLine + '    override fun onResume'
$text2 = $text2.Replace($bad2, $good2)
[IO.File]::WriteAllText($songsPath, $text2)

Write-Host 'Done!'
