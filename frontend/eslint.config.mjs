import js from '@eslint/js';
import pluginVue from 'eslint-plugin-vue';
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting';
import globals from 'globals';

export default [
  {
    ignores: ['dist/**', 'node_modules/**', '.vscode/**', '.idea/**']
  },
  {
    files: ['**/*.{vue,js,jsx,cjs,mjs}'],
    languageOptions: {
      globals: globals.browser,
      ecmaVersion: 'latest'
    }
  },
  js.configs.recommended,
  ...pluginVue.configs['flat/essential'],
  skipFormatting,
  {
    rules: {
      'vue/multi-word-component-names': 'off',
      'vue/no-reserved-component-names': 'off'
    }
  }
];
