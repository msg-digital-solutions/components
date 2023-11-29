#!/usr/bin/env bash

#
# Copyright (C) 2010-2022 Talend Inc. - www.talend.com
#
# This source code is available under agreement available at https://www.talend.com/legal-terms/us-eula
#
# You should have received a copy of the agreement along with this program; if not, write to Talend SA
# 5 rue Salomon de Rothschild - 92150 Suresnes, France
#
#

set -xe

# Prepares the asdf-installed repository's tools so they're usable afterwards.
main() (
  asdf install # Installs all the missing tools in the current folder's .tool-versions file
  asdf reshim # Re-links all the shims (some commands fail when this is not done)
  asdf current # Outputs the currently used tools to the console for debugging purposes
)

main "$@"
