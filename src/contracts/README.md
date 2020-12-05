# Clarity contracts for use with Pietron

The source contract is in piestack.dclr using extensions to the Clarity language.
Use [ClarityTools](https://clarity.tools) to transpile it to Canonical Clarity.
See the transpiled contract in the piestack.clar file.

## PATRONEE INTERFACE 

`(fundraise (address principal))`
Creates a principal to raise funds for the address

`(reassign (address principal) (manager principal))`
Assign a new manager for the fundraising account

## PATRON INTERFACE

`(get-portfolio)`
A list of patronee addresses and their proportional share size to receive 

`(set-portfolio! (value {pie: (list 99 {address: principal, size: int})}))`
Update the portfolio of patronees and their donation proportions

`(donate! (amount int))`
Divide and transfer the amount of funds to the pie of patronees.




 

