import re
import sys

def fix_file(filepath, pattern, replacement):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            text = f.read()
        new_text = re.sub(pattern, replacement, text)
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_text)
        print(f"Fixed {filepath}")
    except Exception as e:
        print(f"Error on {filepath}: {e}")

fname_home = r'c:\Users\ellagoss\AndroidStudioProjects\PanchyFai\app\src\main\java\com\example\panchify\vistas\Home.kt'
# Match any combination of 3 braces followed by override fun onResume
fix_file(fname_home, r'\}\s*\}\s*\}\s*override fun onResume', r'}\n    }\n\n    override fun onResume')

fname_songs = r'c:\Users\ellagoss\AndroidStudioProjects\PanchyFai\app\src\main\java\com\example\panchify\vistas\Songs.kt'
# Match enqueue callbacks closing duplicates
fix_file(fname_songs, r'\}\)\s*\}\)\s*\}\s*override fun onResume', r'})\n    }\n\n    override fun onResume')

print("All done!")
