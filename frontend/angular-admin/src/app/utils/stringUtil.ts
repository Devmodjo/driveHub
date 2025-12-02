/**
 * @author Basile fofack
 * @email juniorbasilefofack@gmail.com
 */
export function extractCityAndStateAndCountryNames(input: string): string {
  const parts: string[] = input.split(/[(),]/);

  let cityName: string = "";
  let stateName: string = "";
  let countryName: string = "";

  parts.forEach((part, i) => {
    const trimmedPart = part.trim();
    switch (i) {
      case 0:
        cityName = trimmedPart;
        break;
      case 1:
        stateName = trimmedPart;
        break;
      case 2:
        countryName = trimmedPart;
        break;
    }
  });

  return `${cityName}/${stateName}/${countryName}`;
}
