# FlowDebug

![Build](https://github.com/tweis/FlowDebug/workflows/Build/badge.svg)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

<!-- Plugin description -->
This PhpStorm plugin provides the mapping between original and proxy classes in projects based on the Flow framework to allow the setting of breakpoints directly in the IDE.
<!-- Plugin description end -->

## Installation

The plugin can be installed via a custom plugin repository. In order to install the plugin, go ahead and follow [these instructions](https://www.jetbrains.com/help/phpstorm/managing-plugins.html#repos).

Repository URL:
```
https://raw.githubusercontent.com/tweis/FlowDebug/main/updatePlugins.xml
```

## Usage

Before the plugin can be used, the project-specific settings must be configured in PhpStorm at: <kbd>Settings/Preferences</kbd> > <kbd>PHP</kbd> > <kbd>Debug</kbd> > <kbd>FlowDebug</kbd>. 

After the configuration of the Flow context and the path to the Data/Temporary directory has been completed, the default user interface of PhpStorm can be used to set breakpoints directly in the IDE.

## Acknowledgments

This project is inspired by the work of [Sandstorm](https://github.com/sandstorm/debugproxy) and [Dominique Feyer](https://github.com/dfeyer/flow-debugproxy) to provide a solution  that makes the usage of Xdebug more convenient in projects based on the Flow framework. 

The goal of this project is to bring a direct integration into PhpStorm for the necessary path mappings between original and proxy classes. This eliminates the need for an additional component such as the debug proxy and enables the use in projects without requiring additional adjustments to the local development environment.