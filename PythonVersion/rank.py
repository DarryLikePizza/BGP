tup_list = [('United States', 157), ('Lebanon', 7), ('Australia', 43), ('Russian Federation', 88), ('Singapore', 65),
 ('Malaysia', 77), ('Germany', 95), ('Sweden', 93), ('Brazil', 87), ('Canada', 24), ('Denmark', 44), ('Italy', 78),
 ('Netherlands', 50), ('Hong Kong', 87), ('Japan', 20), ('Ukraine', 45), ('Thailand', 24), ('Korea, Republic of', 24),
 ('Romania', 55), ('Angola', 16), ('China', 26), ('India', 21), ('Taiwan', 18), ('European Union', 65), ('France', 47),
 ('Philippines', 14), ('Norway', 17), ('Viet Nam', 18), ('Switzerland', 22), ('Palestinian Territory, Occupied', 8),
 ('Spain', 23), ('South Africa', 13), ('Ghana', 1), ('Mauritius', 17), ('Cameroon', 3), ('Finland', 14), ('Turkey', 15),
 ('United Arab Emirates', 24), ('Jordan', 7), ('Argentina', 7), ('Uganda', 2), ('Armenia', 11), ('Burundi', 3),
 ('Tanzania, United Republic of', 2), ('Indonesia', 17), ('Uruguay', 3), ('Venezuela, Bolivarian Republic of', 11),
 ('Bulgaria', 22), ('Israel', 16), ('Qatar', 11), ('Tajikistan', 0), ('Azerbaijan', 13), ('Iraq', 7), ('Poland', 15),
 ('Luxembourg', 10), ('Austria', 28), ('Belgium', 17), ('Lithuania', 12), ('Croatia', 11), ('Ireland', 11),
 ('Slovakia', 12), ('Kazakhstan', 7), ('Georgia', 12), ('Estonia', 10), ('Latvia', 12), ('Slovenia', 13),
 ('Moldova, Republic of', 10), ('Bosnia and Herzegovina', 6), ('Hungary', 14), ('Saudi Arabia', 16), ('Panama', 6),
 ('Costa Rica', 6), ('Czech Republic', 14), ('Portugal', 9), ('Greece', 11), ('Iran, Islamic Republic of', 7),
 ('Syrian Arab Republic', 4), ('Cyprus', 7), ('Oman', 13), ('Serbia', 9), ('Iceland', 8), ('Cambodia', 6),
 ('Macedonia, The Former Yugoslav Republic of', 6), ('Liechtenstein', 3), ('Jersey', 3), ('Egypt', 2), ('Libya', 1),
 ('Yemen', 2), ('Belarus', 8), ('Jamaica', 5), ('Bahrain', 10), ('Kuwait', 2), ('Gibraltar', 2), ('Colombia', 6),
 ('Uzbekistan', 1), ('RÉUNION', 2), ('Guam', 1), ('Dominican Republic', 2), ('Mexico', 5), ('Nigeria', 4), ('Peru', 4),
 ('Pakistan', 12), ('Chile', 5), ('Puerto Rico', 6), ('Mongolia', 9), ('New Zealand', 9), ('Papua New Guinea', 1),
 ('Trinidad and Tobago', 1), ('Macao', 1), ('Ecuador', 4), ('Guatemala', 4), ('Sri Lanka', 2), ('Afghanistan', 0),
 ('Dominica', 2), ('Saint Kitts and Nevis', 0), ('Cayman Islands', 1), ('Bahamas', 4), ('Myanmar', 5), ('Grenada', 2),
 ('Kyrgyzstan', 1), ('Barbados', 1)]

sort_list = sorted(tup_list, key=lambda x: x[1], reverse=True)

print(sort_list)

