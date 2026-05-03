const { execSync } = require('child_process');

try {
  const version = execSync('java -version 2>&1').toString();
  console.log('\x1b[32m[nioflow] Java detected. Ready to use.\x1b[0m');
} catch {
  console.error('\x1b[31m[nioflow] Java 17+ not found. Please install JDK first: https://adoptium.net\x1b[0m');
  process.exit(1);
}
