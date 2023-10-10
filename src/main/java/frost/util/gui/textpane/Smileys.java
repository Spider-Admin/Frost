/*
 Smileys.java / Frost
 Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as
 published by the Free Software Foundation; either version 2 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.util.gui.textpane;

/**
 * Java 1.4 not support enum, use interface here
 * @author ET
 *
 */
public interface Smileys {

	/**
	 * Type of smileys, used for search this in message 
	 */
	public final static String[][] SMILEYS =
	{	{},
		{	":)" , ":-)"	},					// Smile (1)
		{	":(" , ":-("	},					// Sad (2)
		{	";)" , ";-)"	},					// Wink (3)
		{	":D" , ":-D"	},					// Grin (4)
		{	";;)" 			},					// Eyelashes (5)

		{	":-/", ":/"	},						// Confused (6)
		{	":x" , ":X" , ":-x" , ":-X" },		// Love struck (7)
		{	":\">"	},							// Blush (8)
		{	":p" , ":-p", ":P", ":-P"	},					// Tongue (9)
		{	":*" , ":-*"	},					// Kiss (10)

		{	":O" , ":o" , ":-O", ":-o"	},		// Shock (11)
		{	"X-("			},					// Anger (12)
		{	":>" , ":->"	},					// Smug (13)
		{	"B)" , "B-)"	},					// Cool (14)
		{	":s" , ":S" , ":-s" , ":-S "	},		// Worried (15)

		{	">:)"	},							// Devilish (16)
		{	":(("	},							// Crying (17)
		{	":))"	},							// Laughing (18)
		{	":|" , ":-|"	},					// Straight face (19)
		{	"/:)"	},							// Eyebrow (20)

		{	"O:)" , "o:)"	},					// Angel (21)
		{	":-B"	},							// Nerd (22)
		{	"=;"	},							// Talk to the hand (23)
		{	"I-)" , "|-)"	},					// Sleep (24)
		{	"8-|"	},							// Rolling eyes (25)

		{	":-&"	},							// Sick (26)
		{	":-$"	},							// Shhh (27)
		{	"[-("	},							// Not talking (28)
		{	":o)"	},							// Clown (29)
		{	"8-}"	},							// Silly (30)

		{	"(:|"	},							// Tired (31)
		{	"=P~"	},							// Drooling (32)
		{	":-?"	},							// Thinking (33)
		{	"#-o" , "#-O"	},					// D'oh (34)
		{	"=D>"	},							// Applause (35)

		{	":@)"	},							// Pig (36)
		{	"3:-O"	},							// Cow (37)
		{	":(|)"	},							// Monkey (38)
		{	"~:>"	},							// Chicken (39)
		{	"@};-"	},							// Rose (40)

		{	"%%-"	},							// Good luck (41)
		{	"**=="	},							// Flag (42)
		{	"(~~)"	},							// Pumpkin (43)
		{	"~o)"	},							// Coffee (44)
		{	"*-:)"	},							// Idea (45)

		{	"8-X"	},							// Skull (46)
		{	"=:)"	},							// Bug (47)
		{	">-)"	},							// Alien (48)
		{	":-L"	},							// Frustrated (49)
		{	"<):)"	},							// Cowboy (50)

		{	"[-o"	},							// Praying (51)
		{	"<@-)"	},							// Hypnotised (52)
		{	"$-)"	},							// Money eyes (53)
		{	":-\""	},							// Whistling (54)
		{	":^o"	},							// Liar liar (55)

		{	"b-("	},							// Beat up (56)
		{	":)>-"	},							// Peace (57)
		{	"[-X"	},							// Shame on you (58)
		{	"\\:D/"	},							// Dancing (59)
		{	">:D<"	},							// Hugs (60)

		{	"#:-S"	},							// Whew (61)
		{	"=(("	},							// Broken heart (62)
		{	"=))"	},							// R.O.F.L. (63)
		{	"L-)"	},							// Loser (64)
		{	"<:-P"	},							// Party (65)

		{	":-w"	},							// Waiting (66)
		{	":-<"	},							// Sigh (67)
		{	">:P"	},							// Phbbbbt (raspberry) (68)
		{	">:/"	},							// Bring it on (69)
		{	";))"	},							// Tee hee (70)

		{	"^:)^"	},							// Not worthy (71)
		{	":-j"	},							// Go on (72)
		{	"(*)"	}							// Star (73)
	};
}
