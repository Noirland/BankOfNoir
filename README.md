# BankOfNoir #

BankOfNoir creates an economy based around physical currency - by default, gold.

Players balances are stored in banks. The player can add or remove gold from a bank as they wish, which in turn affects their balance (which can be used with any Vault compatible plugin).

This gold balance is not stored as the items themselves - it is dynamically translated to and from a simple decimal balance. A player will see that their items are safe in their bank - in reality, those items got deleted and recreated on using their bank.

## Features ##

* Integration with Vault - all currency is represented as a simple decimal value
* Configurable - you choose what the currency is, and their worth
* Players can choose to use a command or a physical chest to access their bank
  * Either way, the player can see a physical manifestation of their entire balance as the denominations
  * If a bank is open by more than one player, all instances will be kept in sync
* Ability to pay directly into another player's bank
* Administrative commands to view or adjust a player's bank balance
* All changes to balances are instantly backed up to a MySQL server
* Extensible - other plugins can create different banks, such as those used in [NoirGuilds](http://github.com/ZephireNZ/NoirGuilds)
* Full permissions structure, for fine grained control of what players can do

## Creating a bank chest ##

To create a bank chest:

1. Place a standard chest (ie not trapped)
2. Create a sign on the chest with `[Bank]` on the first line
  * This is completely case insensitive, and will ignore all other spacing or colouring - go nuts
3. If all goes well, the bank chest will be created, with your name on the sign.
4. All gold in this chest will be converted to currency and be removed, in line with the plugin's goal. All other non-currency items will be dropped.

## Commands ##

### /bank ###
Gives virtual access to a player's bank, where they can add or remove currency.

All non-currency items will be returned to their inventory or dropped.

### /pay ###

`/pay [player] [amount]`

Pays a player the given amount from the sender's bank.

Automatically checks for whether the player has enough balance, and that the amount is valid.

### /bankadmin ###

`/bankadmin reload`

Reloads the plugin, mainly to refresh config and reconnect with the database.

`/bankadmin see [player]`

Shows the sender a view of the given player's bank.

`/bankadmin adjust [player] [amount]`

Changes the given player's balance by a given amount. This amount can be either negative or positive, but cannot result in a overall negative balance for the player.

## Permissions ##

`bankofnoir.bank` - allows for accessing banks through the command rather than a physical chest

`bankofnoir.pay` - gives access to the pay command

`bankofnoir.admin.reload` - allows for reloading of plugin

`bankofnoir.admin.see` - allows for viewing other player's banks

`bankofnoir.admin.adjust` - allow for adjusting player balances