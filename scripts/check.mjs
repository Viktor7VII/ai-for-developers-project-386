import { spawnSync } from 'node:child_process'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const isWindows = process.platform === 'win32'

function run(label, command, args, cwd) {
  console.log(`\n========== ${label} ==========\n`)

  const result = isWindows
    ? spawnSync(
        process.env.ComSpec || 'cmd.exe',
        ['/d', '/s', '/c', [command, ...args].join(' ')],
        { cwd, stdio: 'inherit' },
      )
    : spawnSync(command, args, { cwd, stdio: 'inherit' })

  if (result.error) {
    console.error(result.error)
  }

  if (result.status !== 0) {
    process.exit(result.status ?? 1)
  }
}

const backend = resolve(root, 'backend')
const frontend = resolve(root, 'frontend')

run(
  'Backend tests',
  isWindows ? 'mvn.cmd' : 'mvn',
  ['test'],
  backend,
)

run(
  'Frontend tests',
  isWindows ? 'npm.cmd' : 'npm',
  ['test'],
  frontend,
)

run(
  'Frontend lint',
  isWindows ? 'npm.cmd' : 'npm',
  ['run', 'lint'],
  frontend,
)

run(
  'Frontend build',
  isWindows ? 'npm.cmd' : 'npm',
  ['run', 'build'],
  frontend,
)

console.log('\nAll project checks passed!')
