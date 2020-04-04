#!/usr/bin/env bash

ansible-playbook --vault-password-file "${HOME}"/creds/vaultpass -i ansible/playbook/hosts ansible/playbook/playbook.yml --extra-vars "env=dev"