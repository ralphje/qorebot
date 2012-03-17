CREATE TABLE IF NOT EXISTS `channels` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `autojoin` tinyint(1) NOT NULL DEFAULT '0',
  `key` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE IF NOT EXISTS `channels_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `channel_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `level` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `channel_id` (`channel_id`,`user_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `commands` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `autoregister_users` int(1) NOT NULL,
  `autoregister_channels` int(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `commands` (`id`, `name`, `autoregister_users`, `autoregister_channels`) VALUES
(1, 'plugins.commands.PluginCommand', 1, 1),
(2, 'plugins.commands.TimeCommand', 1, 1),
(3, 'plugins.commands.IdentifyCommand', 1, 1),
(4, 'plugins.commands.CommandCommand', 1, 1),
(5, 'plugins.commands.OwnerCommand', 0, 0),
(6, 'plugins.commands.OpVoiceCommand', 0, 1),
(7, 'plugins.commands.SayCommand', 0, 1),
(8, 'plugins.commands.HelpCommand', 1, 1),
(9, 'plugins.commands.UserCommand', 1, 1);

CREATE TABLE IF NOT EXISTS `commands_channels` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `command_id` int(11) NOT NULL,
  `channel_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `command_id` (`command_id`,`channel_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `commands_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `command_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `command_id` (`command_id`,`user_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `commands_users` (`id`, `command_id`, `user_id`) VALUES
(1, 5, 1);

CREATE TABLE IF NOT EXISTS `help` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `public` int(1) NOT NULL,
  `code` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `title` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `content` text COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `help`
--

INSERT INTO `help` (`id`, `public`, `code`, `title`, `content`) VALUES
(1, 1, 'syntax', 'Command Syntax', 'To send commands to the bot, you can use exclamation marks (!), quotation marks (") and curly brackets ({,}). Parameters are seperated by spaces, but anything enclosed in quotation marks will be handled as a single parameter. Anything enclosed in brackets will be handled as a subcommand; when you start a parameter with an exclamation mark, this implictly opens a subcommand. For example, ''!say !time "d-M-Y"'' is the same as ''!say {!time "d-M-Y"}''.\r\n\r\nStarting quotation marks are only recognized when they are proceeded by a white space character and closing quotations marks are only recognized when followed by a whitespace character (or at the end of the command). Similarly, exclamation marks must be proceeded by a whitespace character to mark the implicit start of a command.\r\n\r\nYou may escape some special characters (",{,},\\) with a backslash (\\). If you want to say ''!time "d-M-Y"'' literally, you may use ''!say "!time \\"d-M-Y\\""''. However, ''!say "!time "d-M-Y""'' will also work, since the second quotation mark does not mark the end of the parameter (as it''s not followed by whitespace). Note that we are required to use a double closing quotation mark, as the final one is recognized as the end of the argument.\r\n\r\nNote that you can''t escape the exclamation mark with a backslash. Reason for this is that escaping a ''!'' would result in a literal ''!''. However, text starting with a literal ''!'' is always a command. You should use " for escaping !''s. Incorrect syntax is ignored in most cases. If you want to know how your command will be parsed, use the ''!parse <command>'' command. This will show the parsing tree for your command.'),
(2, 0, 'time', 'Time', 'Usage: !time  or  !time <format>\r\nRetrieves the current server time. Note that the Gregorian calendar is used. By default, the format is ''yyyy-MM-dd hh:mm:ss z''. For valid time zones, please refer to !help timeformat.'),
(3, 0, 'timeformat', 'Time Formatting', 'Valid time formatting parameters are: G (era designator)  y (year)  M (month)  w (week in year)  W (week in month)  D (day in year)  d (day in month)  F (day of week in month)  E (day in week)  a (am/pm marker)  H (hour 0-23)  k (hour 1-24)  K (hour 0-11)  h (hour 1-12)  m (minute)  s (second)  S (millisecond)  z (time zone)  Z (time zone RFC 822).\r\nEach parameter can be repeated for different results; for example MMMM wil return the full month name, while MM results in a fixed-length month number.'),
(4, 0, 'default', '', 'Use ''!help <topic>'' for more information about any topic. Available topics can be lised with ''!help topics''.'),
(5, 0, 'plugin', 'Plugin Management', 'Usage: !plugin  or  !plugin reload  or  !plugin install <name> <autoC> <autoU>  or  !plugin [load|unload|add]\r\nProvides tools for managing plugins. Without arguments, will list all currently loaded plugins.  ''reload'' will reload all plugins used in the bot.  ''install'' will install any plugin by its name, while providing whether the plugin should automatically load for all channels and/or users.  ''load''/''unload'' are used for temporarily loading a plugin in the current channel.  ''add'' is used for permanently loading a plugin in the current channel.'),
(6, 0, 'say', 'Say', 'Usage: !say <text>          \r\nLets the bot say the specified text.'),
(7, 0, 'plugins', 'Plugins', 'This bot has support for plugins. Plugins can be installed by placing them in an appropriate folder and installing using the ''!plugin install'' command (see ''!help plugin'' for more information). Plugins can have wide range of features. Examples include plugins that provide OP-permissions to channel operators, plugins for logging and plugins that support the use of commands (which is installed by default, see the ''commands'' topic).'),
(8, 1, 'commands', 'Commands', 'This bot has support for commands. Simply put, these are special pieces of text that are handled by the bot. Commands generally start with an exclamation mark (!) and accept zero or more arguments. Examples include ''!time'' and ''!say "hai"''. All commands that are currently available, can be shown by using ''!commands''. \r\nGenerally, you can find more information about the syntax of a command by putting the name into the help command, for example ''!help say''. However, presence or absence of a help topic does not imply the presence or absence of the command.');

CREATE TABLE IF NOT EXISTS `plugins` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `autoregister_users` int(1) NOT NULL,
  `autoregister_channels` int(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `plugins` (`id`, `name`, `autoregister_users`, `autoregister_channels`) VALUES
(1, 'plugins.CommandPlugin', 1, 1),
(2, 'plugins.OpVoicePlugin', 0, 1);

CREATE TABLE IF NOT EXISTS `plugins_channels` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `plugin_id` int(11) NOT NULL,
  `channel_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `plugin_id` (`plugin_id`,`channel_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `plugins_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `plugin_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `plugin_id` (`plugin_id`,`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `password` varchar(40) COLLATE utf8_unicode_ci NOT NULL,
  `level` int(11) NOT NULL,
  `last_unique_id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `last_unique_id` (`last_unique_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `users` (`id`, `username`, `password`, `level`, `last_unique_id`) VALUES
(1, 'owner', '4cb9c8a8048fd02294477fcb1a41191a', 5, '');
