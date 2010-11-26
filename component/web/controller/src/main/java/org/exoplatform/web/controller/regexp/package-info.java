/**
 * Grammar:
 * extended_reg_exp   -> ERE_branch | extended_reg_exp '|' ERE_branch
 * ERE_branch         -> ERE_expression | ERE_branch ERE_expression
 * ERE_expression     -> one_character_ERE | '^' | '$' | '(' extended_reg_exp ')' | ERE_expression ERE_dupl_symbol
 * one_character_ERE  -> ORD_CHAR | QUOTED_CHAR | '.' | bracket_expression
 * ERE_dupl_symbol    -> '*' | '+' | '?' | '{' DUP_COUNT '}' | '{' DUP_COUNT ',' '}' | '{' DUP_COUNT ',' DUP_COUNT '}'
 *
 * bracket_expression -> '[' matching_list ']' | '[' nonmatching_list ']'
 * matching_list      -> bracket_list
 * nonmatching_list   -> '^' bracket_list
 * bracket_list       -> follow_list | follow_list '-'
 * follow_list        -> expression_term | follow_list expression_term
 * expression_term    -> single_expression | range_expression
 * single_expression  -> end_range | character_class | equivalence_class
 * range_expression   -> start_range end_range | start_range '-'
 * start_range        -> end_range '-'
 * end_range          -> COLL_ELEM | collating_symbol
 * collating_symbol   -> Open_dot COLL_ELEM Dot_close Open_dot META_CHAR Dot_close
 * equivalence_class  -> Open_equal COLL_ELEM Equal_close
 * character_class    -> Open_colon class_name Colon_close
 *
 * ORD_CHAR : A character, other than one of the special characters in SPEC_CHAR
 * SPEC_CHAR : '^' '.' '[' '$' '(' ')' '|' '*' '+'  '?' '{' '\'
 * QUOTED_CHAR : '\^' '\.' '\[' '\$' '\(' '\)' '\|' '\*' '\+' '\?' '\{' '\\'
 * DUP_COUNT : Represents a numeric constant. This token will only be recognised when the context of the grammar
 * requires it. At all other times, digits not preceded by '\' will be treated as ORD_CHAR
 * COLL_ELEM : Any single-character collating element, unless it is a META_CHAR
 * META_CHAR :
 *  '^' when found first in a bracket expression
 *  '-' when found anywhere but first (after an initial '^', if any) or last in a bracket expression, or as the ending
 *      range point in a range expression
 *  ']' when found anywhere but first (after an initial '^' if any) in a bracket expression
 *
 * Open_dot : '[.'
 * Dot_close : '.]'
 * Open_equal : '[='
 * Equal_close : '=]'
 * Open_colon : '[:'
 * Colon_close : ':]'
 */
package org.exoplatform.web.controller.regexp;