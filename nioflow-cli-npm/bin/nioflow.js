#!/usr/bin/env node
const { execFileSync } = require('child_process');
const path = require('path');
const jarPath = path.join(__dirname, '..', 'lib', 'nioflow-cli.jar');

try {
  execFileSync('java', ['-jar', jarPath, ...process.argv.slice(2)], {
    stdio: 'inherit'
  });
} catch (e) {
  process.exit(e.status || 1);
}
