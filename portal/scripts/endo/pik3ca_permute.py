import random
import re

NUM_TRIALS = 1000000
NUM_PATIENTS = 9
PIK3CA_PROTEIN_LENGTH = 1068

def isMutationInHelicalDomain(location):
	if (location >= 517 and location <= 694):
		return True
	else:
		return False;

# Read in PIK3CA Mutations in Singly Mutated Cases
file = open ("/Users/ceramie/SugarSync/endo/data/pik3ca/pik3ca_single_mut.txt")
aa_location_list = []
for line in file:
	if not line.startswith("CASE_ID"):
		line = line.strip()
		parts = line.split("\t")
		aa_change = parts[3]
		if aa_change == "p.463_465GSN>D":
			aa_location = 463
		else:
			aa_location = int(re.sub(r'\D+', '', aa_change))
		aa_location_list.append(aa_location)

# Perform Simulation
trial_counter = 0
for i in range(0,NUM_TRIALS):
	num_cases_with_helical_domain_mutation = 0
	for j in range(0, NUM_PATIENTS):
		mutation1Index = random.randrange(0,len(aa_location_list))
		mutation2Index = random.randrange(0,len(aa_location_list))
		mutation1Location = aa_location_list[mutation1Index]
		mutation2Location = aa_location_list[mutation2Index]
		mutation1InHelicalDomain = isMutationInHelicalDomain(mutation1Location)
		mutation2InHelicalDomain = isMutationInHelicalDomain(mutation2Location)
		#print "%d\t%d\t%s\t%s" % (mutation1Location, mutation2Location, 
		#	mutation1InHelicalDomain, mutation2InHelicalDomain)
		if (mutation1InHelicalDomain or mutation2InHelicalDomain):
			num_cases_with_helical_domain_mutation += 1
	if num_cases_with_helical_domain_mutation == 0:
		trial_counter +=1

p_value = trial_counter / float(NUM_TRIALS)
print "Number of times that we get 0 helical domain mutations:  %d." % trial_counter
print "p-value:  %.6f." % p_value
