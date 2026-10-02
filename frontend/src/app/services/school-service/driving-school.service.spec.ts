import { TestBed } from '@angular/core/testing';

import { DrivingSchoolService } from './driving-school.service';

describe('DrivingSchoolService', () => {
  let service: DrivingSchoolService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DrivingSchoolService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
