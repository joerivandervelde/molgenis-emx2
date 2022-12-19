import gql from "graphql-tag";
export default gql`
  query Subcohort($pid: String, $name: String) {
    Subcohorts(
      filter: {
        resource: { pid: { equals: [$pid] } }
        name: { equals: [$name] }
      }
    ) {
      resource {
        name
      }
      name
      description
      numberOfParticipants
      inclusionStart
      inclusionEnd
      ageGroups {
        name
        code
        order
        parent {
          name
          code
          parent {
            name
            code
          }
          parent {
            name
            code
          }
        }
      }
      mainMedicalCondition {
        name
      }
      countries {
        name
      }
      regions {
        name
      }
      inclusionCriteria
      supplementaryInformation
      comorbidity {
        name
      }
      counts {
        ageGroup {
          name
        }
        N_female
        N_male
        N_total
      }
    }
  }
`;