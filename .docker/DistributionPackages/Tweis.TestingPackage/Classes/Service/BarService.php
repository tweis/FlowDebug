<?php

namespace Tweis\TestingPackage\Service;

use Neos\Flow\Annotations as Flow;

class BarService
{
    public function getBar(): string
    {
        return 'Bar';
    }
}
